/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2018 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu;

import static org.eclipse.tahu.message.model.MetricDataType.Boolean;
import static org.eclipse.tahu.message.model.MetricDataType.Int64;
import static org.eclipse.tahu.message.model.MetricDataType.String;

import java.util.Date;
import java.util.Random;
import java.util.TreeMap;
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
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.PropertyDataType;
import org.eclipse.tahu.message.model.PropertySet.PropertySetBuilder;
import org.eclipse.tahu.message.model.PropertyValue;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

/**
 * An example Sparkplug B application.
 */
public class SparkplugRecordsExample implements MqttCallbackExtended {

	private static final String NAMESPACE = "spBv1.0";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private String serverUrl = "tcp://localhost:1883";
	private String groupId = "Sparkplug B Devices";
	private String edgeNode = "Java Sparkplug B Example";
	private String deviceId = "SparkplugBExample";
	private String clientId = "SparkplugBRecordExample";
	private String username = "admin";
	private String password = "changeme";
	private long eventPeriod = 1000; // Publish period in milliseconds
	private int numOfEventsPerPublish = 10;
	private ExecutorService executor;
	private MqttClient client;

	private int bdSeq = 0;
	private int seq = 0;

	private Object seqLock = new Object();

	public static void main(String[] args) {
		SparkplugRecordsExample example = new SparkplugRecordsExample();
		example.run();
	}

	public void run() {
		try {
			// Random generator and thread pool for outgoing published messages
			executor = Executors.newFixedThreadPool(1);

			// Build up DEATH payload - note DEATH payloads don't have a regular sequence number
			SparkplugBPayloadBuilder deathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date());
			deathPayload = addBdSeqNum(deathPayload);
			byte[] deathBytes = new SparkplugBPayloadEncoder().getBytes(deathPayload.createPayload());

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
			client.setTimeToWait(2000);
			client.setCallback(this); // short timeout on failure to connect
			client.connect(options);

			// Subscribe to control/command messages for both the edge of network node and the attached devices
			client.subscribe(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode + "/#", 0);
			client.subscribe(NAMESPACE + "/" + groupId + "/DCMD/" + edgeNode + "/#", 0);

			// Delay before starting to publish records
			Thread.sleep(3000);
			
			// Loop forever publishing records
			while (true) {
				String recordType = "deviceEvents";
				// Create the payload
				SparkplugBPayload payload = new SparkplugBPayloadBuilder(getSeqNum()).setTimestamp(new Date())
						.setUuid(newUUID()).createPayload();
				
				// Add records to payload
				for (int i = 0; i < numOfEventsPerPublish; i++) {
					Thread.sleep(eventPeriod);
					payload.addMetric(newRecord(recordType));
				}

				// Publish the payload, if connected
				if (client.isConnected()) {
					synchronized (seqLock) {
						System.out.println("Connected - publishing new records");
						client.publish(NAMESPACE + "/" + groupId + "/DRECORD/" + edgeNode + "/" + deviceId,
								new SparkplugBPayloadEncoder().getBytes(payload), 0, false);
					}
				} else {
					System.out.println("Not connected - not publishing records");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Returns a new metric representing a record.
	 */
	private Metric newRecord(String type) throws SparkplugInvalidTypeException {
		Random random = new Random();
		Date timestamp = new Date();

		System.out.println("Creatine new " + type + " record, " + timestamp);
		// Metric name = Record type
		// Metric datatype = (not used)
		// Metric value = (not used)
		// Metric properties = Record fields
		return new MetricBuilder(type, String, null)
				.timestamp(timestamp)
				.properties(new PropertySetBuilder(new TreeMap<>()) // TreeMap for natural ordering of the fields
						.addProperty("intField", new PropertyValue(PropertyDataType.PropertySet,
								new PropertySetBuilder(new TreeMap<>())
										.addProperty("fieldValue",
												new PropertyValue(PropertyDataType.Int32, random.nextInt()))
										.createPropertySet()))
						.addProperty("fltField",
								new PropertyValue(PropertyDataType.PropertySet,
										new PropertySetBuilder(new TreeMap<>())
												.addProperty("fieldValue",
														new PropertyValue(PropertyDataType.Float, random.nextFloat()))
												.createPropertySet()))
						.addProperty("strField",
								new PropertyValue(PropertyDataType.PropertySet,
										new PropertySetBuilder(new TreeMap<>())
												.addProperty("fieldValue",
														new PropertyValue(PropertyDataType.String, newUUID()))
												.createPropertySet()))
						.createPropertySet())
				.createMetric();
	}

	private void publishBirth() {
		publishNodeBirth();
		publishDeviceBirth();
	}

	private void publishNodeBirth() {
		try {
			synchronized (seqLock) {
				// Reset the sequence number
				seq = 0;

				// Create the BIRTH payload
				SparkplugBPayload payload = new SparkplugBPayloadBuilder(getSeqNum()).setTimestamp(new Date())
						.setUuid(newUUID()).addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric())
						.addMetric(new MetricBuilder("Node Control/Rebirth", Boolean, false).createMetric())
						.createPayload();

				System.out.println("Publishing Edge Node Birth");
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payload));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publishDeviceBirth() {
		try {
			synchronized (seqLock) {
				// Create the payload
				SparkplugBPayload payload = new SparkplugBPayloadBuilder(getSeqNum()).setTimestamp(new Date())
						.setUuid(newUUID())
						.addMetric(new MetricBuilder("Device Control/Rebirth", Boolean, false).createMetric())
						.createPayload();

				System.out.println("Publishing Device Birth");
				executor.execute(
						new Publisher(NAMESPACE + "/" + groupId + "/DBIRTH/" + edgeNode + "/" + deviceId, payload));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 *  Add the birth/death sequence number to a payload
	 */
	private SparkplugBPayloadBuilder addBdSeqNum(SparkplugBPayloadBuilder payload) throws Exception {
		if (payload == null) {
			payload = new SparkplugBPayloadBuilder();
		}
		if (bdSeq == 256) {
			bdSeq = 0;
		}
		payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
		bdSeq++;
		return payload;
	}

	/*
	 *  Increments and returns the next sequence number
	 */
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
		if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("NCMD")
				&& splitTopic[3].equals(edgeNode)) {
			for (Metric metric : inboundPayload.getMetrics()) {
				if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean) metric.getValue())) {
					publishBirth();
				} else {
					System.out.println("Unknown Node Command NCMD: " + metric.getName());
				}
			}
		} else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
				&& splitTopic[3].equals(edgeNode)) {
			System.out.println("Command recevied for device " + splitTopic[4]);
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Published message: " + token);
	}

	private String newUUID() {
		return java.util.UUID.randomUUID().toString();
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
