/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Cirrus Link Solutions - initial implementation
 ********************************************************************************/

package org.eclipse.tahu;

import static org.eclipse.tahu.message.model.MetricDataType.Boolean;
import static org.eclipse.tahu.message.model.MetricDataType.Int32;
import static org.eclipse.tahu.message.model.MetricDataType.Int64;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

/**
 * An example Sparkplug B application.
 */
public class SparkplugExample implements MqttCallbackExtended {
	
	private static final String NAMESPACE = "spBv1.0";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private String serverUrl = "tcp://192.168.1.53:1883";
	private String groupId = "Sparkplug B Devices";
	private String edgeNode = "Java Sparkplug B Example";
	private String deviceId = "SparkplugBExample";
	private String clientId = "SparkplugBExampleEdgeNode";
	private String username = "admin";
	private String password = "changeme";
	private long PUBLISH_PERIOD = 1;					// Publish period in milliseconds
	private ExecutorService executor;
	private MqttClient client;
	
	private int index = 0;
	private Calendar calendar = Calendar.getInstance();
	
	private int bdSeq = 0;
	private int seq = 0;
	
	private Object seqLock = new Object();
	
	public static void main(String[] args) {
		SparkplugExample example = new SparkplugExample();
		example.run();
	}
	
	public void run() {
		try {
			// Random generator and thread pool for outgoing published messages
			executor = Executors.newFixedThreadPool(1);
			
			// Build up DEATH payload - note DEATH payloads don't have a regular sequence number
			SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());
			deathPayload = addBdSeqNum(deathPayload);
			byte [] deathBytes = new SparkplugBPayloadEncoder().getBytes(deathPayload.createPayload());
			
			MqttConnectOptions options = new MqttConnectOptions();
			
			if (USING_REAL_TLS) {
				SocketFactory sf = SSLSocketFactory.getDefault();
				options.setSocketFactory(sf);
			}
			
			// Connect to the MQTT Server
			options.setAutomaticReconnect(true);
			options.setCleanSession(true);
			options.setConnectionTimeout(30);
			options.setKeepAliveInterval(30);
			options.setUserName(username);
			options.setPassword(password.toCharArray());
			options.setWill(NAMESPACE + "/" + groupId + "/NDEATH/" + edgeNode, deathBytes, 0, false);
			client = new MqttClient(serverUrl, clientId);
			client.setTimeToWait(30000);	
			client.setCallback(this);					// short timeout on failure to connect
			client.connect(options);
			
			// Subscribe to control/command messages for both the edge of network node and the attached devices
			client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
			client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);
			

			List<Metric> nodeMetrics = new ArrayList<Metric>();
			List<Metric> deviceMetrics = new ArrayList<Metric>();
			
			// Loop forever publishing data every PUBLISH_PERIOD
			while (true) {
				Thread.sleep(PUBLISH_PERIOD);
				
				synchronized(seqLock) {
					if (client.isConnected()) {

						System.out.println("Time: " + calendar.getTimeInMillis() + "  Index: " + index);
						
						// Add a 'real time' metric
						nodeMetrics.add(new MetricBuilder("MyNodeMetric", Int32, index)
								.timestamp(calendar.getTime())
								.createMetric());

						// Add a 'real time' metric
						deviceMetrics.add(new MetricBuilder("MyDeviceMetric", Int32, index+50)
								.timestamp(calendar.getTime())
								.createMetric());

						// Publish, increment the calendar and index and reset
						calendar.add(Calendar.MILLISECOND, 1);
						if (index == 50) {
							index = 0;
							
							System.out.println("nodeMetrics: " + nodeMetrics.size());
							System.out.println("deviceMetrics: " + deviceMetrics.size());

							SparkplugBPayload nodePayload = new SparkplugBPayload(
									new Date(), 
									nodeMetrics, 
									getSeqNum(),
									null, 
									null);
							
							client.publish(NAMESPACE + "/" + groupId + "/NDATA/" + edgeNode, 
									new SparkplugBPayloadEncoder().getBytes(nodePayload), 0, false);
							
							SparkplugBPayload devicePayload = new SparkplugBPayload(
									new Date(),
									deviceMetrics,
									getSeqNum(),
									null, 
									null);

							client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, 
									new SparkplugBPayloadEncoder().getBytes(devicePayload), 0, false);
							
							nodeMetrics = new ArrayList<Metric>();
							deviceMetrics = new ArrayList<Metric>();
						} else {
							index++;
						}
					} else {
						System.out.println("Not connected - not publishing data");
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void publishBirth() {
		try {
			synchronized(seqLock) {
				// Reset the sequence number
				seq = 0;
				
				// Reset the index and time
				index = 0;
				calendar = Calendar.getInstance();
				
				// Create the BIRTH payload and set the position and other metrics
				SparkplugBPayload payload = new SparkplugBPayload(
						calendar.getTime(), 
						new ArrayList<Metric>(), 
						getSeqNum(),
						null, 
						null);
				
				payload.addMetric(new MetricBuilder("bdSeq", Int64, (long)bdSeq).createMetric());		
				payload.addMetric(new MetricBuilder("Node Control/Rebirth", Boolean, false)
						.createMetric());
	
				payload.addMetric(new MetricBuilder("MyNodeMetric", Int32, index)
						.timestamp(calendar.getTime())
						.createMetric());
				
				System.out.println("Publishing Edge Node Birth");
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payload));
	
				// Create the payload and add a metric
				payload = new SparkplugBPayload(
						calendar.getTime(),
						new ArrayList<Metric>(),
						getSeqNum(),
						null, 
						null);
				
				payload.addMetric(new MetricBuilder("MyDeviceMetric", Int32, index+50)
						.timestamp(calendar.getTime())
						.createMetric());
				
				System.out.println("Publishing Device Birth");
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/DBIRTH/" + edgeNode + "/" + deviceId, payload));
				
				// Increment the global vars
				calendar.add(Calendar.MILLISECOND, 1);
				index++;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// Used to add the birth/death sequence number
	private SparkplugBPayloadBuilder addBdSeqNum(SparkplugBPayloadBuilder payload) throws Exception {
		if (payload == null) {
			payload = new SparkplugBPayloadBuilder();
		}
		if (bdSeq == 256) {
			bdSeq = 0;
		}
		payload.addMetric(new MetricBuilder("bdSeq", Int64, (long)bdSeq).createMetric());
		bdSeq++;
		return payload;
	}
	
	// Used to add the sequence number
	private long getSeqNum() throws Exception {
		System.out.println("seq: " + seq);
		if (seq == 256) {
			seq = 0;
		}
		return seq++;
	}
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		System.out.println("Connected! - publishing birth");
		publishBirth();
	}

	public void connectionLost(Throwable cause) {
		cause.printStackTrace();
		System.out.println("The MQTT Connection was lost! - will auto-reconnect");
    }

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Message Arrived on topic " + topic);
		
		SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
		SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());
		
		// Debug
		for (Metric metric : inboundPayload.getMetrics()) {
			System.out.println("Metric " + metric.getName() + "=" + metric.getValue());
		}
		
		String[] splitTopic = topic.split("/");
		if (splitTopic[0].equals(NAMESPACE) && 
				splitTopic[1].equals(groupId) &&
				splitTopic[2].equals("NCMD") && 
				splitTopic[3].equals(edgeNode)) {
			for (Metric metric : inboundPayload.getMetrics()) {
				if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean)metric.getValue())) {
					publishBirth();
				} else {
					System.out.println("Unknown Node Command NCMD: " + metric.getName());
				}
			}
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Published message: " + token);
	}
	
	private class Publisher implements Runnable {
		
		private String topic;
		private SparkplugBPayload outboundPayload;

		public Publisher(String topic, SparkplugBPayload outboundPayload) {
			this.topic = topic;
			this.outboundPayload = outboundPayload;
		}
		
		public void run() {
			try {
				outboundPayload.setTimestamp(new Date());
				SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
				client.publish(topic, encoder.getBytes(outboundPayload), 0, false);
			} catch (MqttPersistenceException e) {
				e.printStackTrace();
			} catch (MqttException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
}
