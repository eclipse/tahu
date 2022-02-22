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
import static org.eclipse.tahu.message.model.MetricDataType.DateTime;
import static org.eclipse.tahu.message.model.MetricDataType.Double;
import static org.eclipse.tahu.message.model.MetricDataType.Float;
import static org.eclipse.tahu.message.model.MetricDataType.Int16;
import static org.eclipse.tahu.message.model.MetricDataType.Int32;
import static org.eclipse.tahu.message.model.MetricDataType.Int64;
import static org.eclipse.tahu.message.model.MetricDataType.Int8;
import static org.eclipse.tahu.message.model.MetricDataType.String;
import static org.eclipse.tahu.message.model.MetricDataType.Template;
import static org.eclipse.tahu.message.model.MetricDataType.Text;
import static org.eclipse.tahu.message.model.MetricDataType.UInt16;
import static org.eclipse.tahu.message.model.MetricDataType.UInt32;
import static org.eclipse.tahu.message.model.MetricDataType.UInt64;
import static org.eclipse.tahu.message.model.MetricDataType.UInt8;
import static org.eclipse.tahu.message.model.MetricDataType.UUID;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

/**
 * An example Sparkplug B application.
 */
public class SparkplugExample implements MqttCallbackExtended {

	private static final String NAMESPACE = "spBv1.0";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private String serverUrl = "tcp://localhost:1883";
	private String groupId = "Sparkplug B Devices";
	private String edgeNode = "Java Sparkplug B UDT Example";
	private String deviceId = "SparkplugBExample";
	private String clientId = "SparkplugBExampleEdgeNode";
	private String username = "admin";
	private String password = "changeme";
	private long PUBLISH_PERIOD = 60000; // Publish period in milliseconds
	private ExecutorService executor;
	private MqttClient client;

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

			// Loop forever publishing data every PUBLISH_PERIOD
			while (true) {
				Thread.sleep(PUBLISH_PERIOD);

				if (client.isConnected()) {
					synchronized (seqLock) {
						System.out.println("Connected - publishing new data");
						// Create the payload and add some metrics
						SparkplugBPayload payload = new SparkplugBPayload(new Date(), newComplexTemplateInstance(),
								getSeqNum(), newUUID(), null);

						client.publish(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId,
								new SparkplugBPayloadEncoder().getBytes(payload), 0, false);
					}
				} else {
					System.out.println("Not connected - not publishing data");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publishBirth() {
		try {
			synchronized (seqLock) {
				// Reset the sequence number
				seq = 0;

				// Create the BIRTH payload and set the position and other metrics
				SparkplugBPayload payload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(),
						newUUID(), null);

				payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
				payload.addMetric(new MetricBuilder("Node Control/Rebirth", Boolean, false).createMetric());

				// Add a node level template definition and instance
				payload.addMetric(
						new MetricBuilder("simpleType", Template, newSimpleTemplate(true, null)).createMetric());
				payload.addMetric(new MetricBuilder("mySimpleType", Template, newSimpleTemplate(false, "simpleType"))
						.createMetric());
				
				// Add the complex template definition - All UDT definitions must be published in the NBIRTH
				payload.addMetrics(newComplexTemplateDefs());

				System.out.println("Publishing Edge Node Birth");
				executor.execute(new Publisher(NAMESPACE + "/" + groupId + "/NBIRTH/" + edgeNode, payload));

				// Create the payload and add a complex Template instance
				payload = new SparkplugBPayload(new Date(), newComplexTemplateInstance(), getSeqNum(), newUUID(), null);

				System.out.println("Publishing Device Birth");
				executor.execute(
						new Publisher(NAMESPACE + "/" + groupId + "/DBIRTH/" + edgeNode + "/" + deviceId, payload));
			}
		} catch (Exception e) {
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
		payload.addMetric(new MetricBuilder("bdSeq", Int64, (long) bdSeq).createMetric());
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
		if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("NCMD")
				&& splitTopic[3].equals(edgeNode)) {
			for (Metric metric : inboundPayload.getMetrics()) {
				if ("Node Control/Rebirth".equals(metric.getName()) && ((Boolean) metric.getValue())) {
					publishBirth();
				} else {
					// TODO
					System.out.println("TODO - handle writes to tag: " + metric.getName());
				}
			}
		} else if (splitTopic[0].equals(NAMESPACE) && splitTopic[1].equals(groupId) && splitTopic[2].equals("DCMD")
				&& splitTopic[3].equals(edgeNode)) {
			System.out.println("Command recevied for device " + splitTopic[4]);

			SparkplugBPayload outboundPayload = new SparkplugBPayload(new Date(), new ArrayList<Metric>(), getSeqNum(),
					newUUID(), null);
			for (Metric metric : inboundPayload.getMetrics()) {
				// TODO
				System.out.println("TODO - handle writes to tag: " + metric.getName());
			}

			// Publish the message in a new thread
			executor.execute(
					new Publisher(NAMESPACE + "/" + groupId + "/DDATA/" + edgeNode + "/" + deviceId, outboundPayload));
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Published message: " + token);
	}

	private String newUUID() {
		return java.util.UUID.randomUUID().toString();
	}

	private List<Parameter> newParams() throws SparkplugException {
		Random random = new Random();
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new Parameter("ParamInt32", ParameterDataType.Int32, random.nextInt()));
		params.add(new Parameter("ParamFloat", ParameterDataType.Float, random.nextFloat()));
		params.add(new Parameter("ParamDouble", ParameterDataType.Double, random.nextDouble()));
		params.add(new Parameter("ParamBoolean", ParameterDataType.Boolean, random.nextBoolean()));
		params.add(new Parameter("ParamString", ParameterDataType.String, newUUID()));
		return params;
	}

	private List<Metric> newComplexTemplateDefs() throws SparkplugInvalidTypeException {
		ArrayList<Metric> metrics = new ArrayList<Metric>();
		
			// Add a new template "subType" definition with two primitive members
			metrics.add(new MetricBuilder("subType", Template,
					new TemplateBuilder().definition(true)
							.addMetric(new MetricBuilder("StringMember", String, "value").createMetric())
							.addMetric(new MetricBuilder("IntegerMember", Int32, 0).createMetric()).createTemplate())
									.createMetric());
			// Add new template "complexType" definition that contains an instance of "subType" as a member
			metrics.add(new MetricBuilder("complexType", Template,
					new TemplateBuilder().definition(true)
							.addMetric(new MetricBuilder("mySubType", Template,
									new TemplateBuilder().definition(false).templateRef("subType")
											.addMetric(
													new MetricBuilder("StringMember", String, "value").createMetric())
											.addMetric(new MetricBuilder("IntegerMember", Int32, 0).createMetric())
											.createTemplate()).createMetric())
							.createTemplate()).createMetric());
		
		return metrics;

	}

	private List<Metric> newComplexTemplateInstance() throws SparkplugInvalidTypeException {
		ArrayList<Metric> metrics = new ArrayList<Metric>();

		// Add an instance of "complexType
		metrics.add(
				new MetricBuilder("myNewType", Template,
						new TemplateBuilder().definition(false).templateRef("complexType")
								.addMetric(new MetricBuilder("mySubType", Template,
										new TemplateBuilder().definition(false).templateRef("subType")
												.addMetric(
														new MetricBuilder("StringMember", String, "myValue")
																.createMetric())
												.addMetric(new MetricBuilder("IntegerMember", Int32, 1).createMetric())
												.createTemplate()).createMetric())
								.createTemplate()).createMetric());

		return metrics;
	}
	private Template newSimpleTemplate(boolean isDef, String templatRef) throws SparkplugException {
		Random random = new Random();
		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(new MetricBuilder("MyInt8", Int8, (byte) random.nextInt()).createMetric());
		metrics.add(new MetricBuilder("MyInt16", Int16, (short) random.nextInt()).createMetric());
		metrics.add(new MetricBuilder("MyInt32", Int32, random.nextInt()).createMetric());
		metrics.add(new MetricBuilder("MyInt64", Int64, random.nextLong()).createMetric());
		metrics.add(new MetricBuilder("MyUInt8", UInt8, (short) random.nextInt()).createMetric());
		metrics.add(new MetricBuilder("MyUInt16", UInt16, random.nextInt()).createMetric());
		metrics.add(new MetricBuilder("MyUInt32", UInt32, random.nextLong()).createMetric());
		metrics.add(new MetricBuilder("MyUInt64", UInt64, BigInteger.valueOf(random.nextLong())).createMetric());
		metrics.add(new MetricBuilder("MyFloat", Float, random.nextFloat()).createMetric());
		metrics.add(new MetricBuilder("MyDouble", Double, random.nextDouble()).createMetric());
		metrics.add(new MetricBuilder("MyBoolean", Boolean, random.nextBoolean()).createMetric());
		metrics.add(new MetricBuilder("MyString", String, newUUID()).createMetric());
		metrics.add(new MetricBuilder("MyDateTime", DateTime, new Date()).createMetric());
		metrics.add(new MetricBuilder("MyText", Text, newUUID()).createMetric());
		metrics.add(new MetricBuilder("MyUUID", UUID, newUUID()).createMetric());

		return new TemplateBuilder().version("v1.0").templateRef(templatRef).definition(isDef)
				.addParameters(newParams()).addMetrics(metrics).createTemplate();
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
