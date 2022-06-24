/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.SparkplugParsingException;
import org.eclipse.tahu.edge.api.MetricHandler;
import org.eclipse.tahu.edge.sim.DataSimulator;
import org.eclipse.tahu.edge.sim.RandomDataSimulator;
import org.eclipse.tahu.message.PayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap.SparkplugBPayloadMapBuilder;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.message.model.SparkplugMeta;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.ClientCallback;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;
import org.eclipse.tahu.util.SparkplugUtil;
import org.eclipse.tahu.util.TopicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeNode implements Runnable, MetricHandler, ClientCallback {

	private static Logger logger = LoggerFactory.getLogger(EdgeNode.class.getName());

	private static final String GROUP_ID = "G1";
	private static final String EDGE_NODE_ID = "E1";
	private static final EdgeNodeDescriptor EDGE_NODE_DESCRIPTOR = new EdgeNodeDescriptor(GROUP_ID, EDGE_NODE_ID);
	private static final List<String> DEVICE_IDS = Arrays.asList("D1", "D2", "D3");
	private static final String PRIMARY_HOST_ID = "IamHost";
	private static final Long REBIRTH_DEBOUNCE_DELAY = 5000L;
	private static final String MQTT_CLIENT_ID = "Sparkplug-Tahu-Compatible-Impl";
	private static final MqttServerName MQTT_SERVER_NAME = new MqttServerName("My Mqtt Server");
	private static final MqttServerUrl MQTT_SERVER_URL = new MqttServerUrl("tcp://localhost:1883");
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "changeme";
	private static final int KEEP_ALIVE = 30;
	private static final Topic NDEATH_TOPIC =
			new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX, GROUP_ID, EDGE_NODE_ID, MessageType.NDEATH);

	/*
	 * Next Birth BD sequence number - same as last deathBdSeq
	 */
	private long birthBdSeq = 0;

	/*
	 * Next Death BD sequence number
	 */
	private long deathBdSeq = 0;

	private final DataSimulator dataSimulator =
			new RandomDataSimulator(10, new HashMap<SparkplugDescriptor, Integer>() {

				private static final long serialVersionUID = 1L;

				{
					put(new DeviceDescriptor("G1/E1/D1"), 50);
					put(new DeviceDescriptor("G1/E1/D2"), 50);
					put(new DeviceDescriptor("G1/E1/D3"), 50);
				}
			});

	/*
	 * Lock for manipulating the sequence number
	 */
	private Object clientLock = new Object();

	public static void main(String[] arg) {
		try {
			EdgeNode edgeNode = new EdgeNode();
			Thread edgeNodeThread = new Thread(edgeNode);
			edgeNodeThread.start();

			// Run for a while and shutdown
			Thread.sleep(300000);
			edgeNode.shutdown();
		} catch (Exception e) {
			logger.error("Failed to run the Edge Node", e);
		}
	}

	private EdgeClient edgeClient;
	private Thread edgeClientThread;
	private PeriodicPublisher periodicPublisher;
	private Thread periodicPublisherThread;

	public EdgeNode() {
		try {
			edgeClient = new EdgeClient(this, EDGE_NODE_DESCRIPTOR, DEVICE_IDS, PRIMARY_HOST_ID, REBIRTH_DEBOUNCE_DELAY,
					new MqttClientId(MQTT_CLIENT_ID, false), MQTT_SERVER_NAME, MQTT_SERVER_URL, USERNAME, PASSWORD,
					KEEP_ALIVE, this, null);
		} catch (Exception e) {
			logger.error("Failed to create the Sparkplug Edge Client", e);
		}
	}

	@Override
	public void run() {
		edgeClientThread = new Thread(edgeClient);
		edgeClientThread.start();
	}

	// MetricHandler API
	@Override
	public Topic getDeathTopic() {
		return NDEATH_TOPIC;
	}

	// MetricHandler API
	@Override
	public byte[] getDeathPayloadBytes() throws Exception {
		SparkplugBPayload nDeathPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date()).createPayload();
		addDeathSeqNum(nDeathPayload);
		return new SparkplugBPayloadEncoder().getBytes(nDeathPayload);
	}

	// MetricHandler API
	@Override
	public void publishBirthSequence() {
		try {
			SparkplugBPayloadMap nBirthPayload = dataSimulator.getNodeBirthPayload(EDGE_NODE_DESCRIPTOR);
			nBirthPayload = addBirthSeqNum(nBirthPayload);
			edgeClient.publishNodeBirth(nBirthPayload);

			for (String deviceId : DEVICE_IDS) {
				SparkplugBPayload dBirthPayload =
						dataSimulator.getDeviceBirthPayload(new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, deviceId));
				edgeClient.publishDeviceBirth(deviceId, dBirthPayload);
			}

			// The BIRTH sequence has been published - set up a periodic publisher
			periodicPublisher = new PeriodicPublisher(5000, dataSimulator, edgeClient,
					Arrays.asList(new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, "D1"),
							new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, "D2"),
							new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, "D3")));
			periodicPublisherThread = new Thread(periodicPublisher);
			periodicPublisherThread.start();
		} catch (Exception e) {
			logger.error("Failed to publish the BIRTH sequence", e);
		}
	}

	// MetricHandler API
	@Override
	public boolean hasMetric(SparkplugDescriptor sparkplugDescriptor, String metricName) {
		return dataSimulator.hasMetric(sparkplugDescriptor, metricName);
	}

	// ClientCallback API
	@Override
	public void shutdown() {
		logger.info("ClientCallback shutdown");
		periodicPublisher.shutdown();
		periodicPublisherThread.interrupt();
		periodicPublisher = null;
		periodicPublisherThread = null;

		edgeClient.shutdwon();
		edgeClient = null;
		edgeClientThread = null;
	}

	// ClientCallback API
	@Override
	public void messageArrived(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			String rawTopic, MqttMessage message) {
		logger.info("{}: ClientCallback messageArrived on topic={}", clientId, rawTopic);

		if (rawTopic.startsWith("STATE/")) {
			logger.info("Got STATE message: {} :: {}", rawTopic, new String(message.getPayload()));
			edgeClient.handleStateMessage(rawTopic.substring(6), new String(message.getPayload()));
			return;
		} else if (!SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX.equals(TopicUtil.getSplitTopic(rawTopic)[0])) {
			logger.warn("Message received on erroneous topic: {}", rawTopic);
			return;
		} else {
			// Sparkplug message!
			final Topic topic;
			final SparkplugBPayload payload;
			try {
				topic = TopicUtil.parseTopic(rawTopic);
			} catch (SparkplugParsingException e) {
				logger.error("Error parsing Sparkplug topic {}", rawTopic, e);
				return;
			}

			try {
				// Handling case where the MQTT Server publishes an LWT on our behalf but we're actually online.
				if (MessageType.NDEATH.equals(topic.getType()) && topic.getGroupId().equals(GROUP_ID)
						&& topic.getEdgeNodeId().equals(EDGE_NODE_ID)) {
					if (!edgeClient.isDisconnectedOrDisconnecting()) {
						if (edgeClient.isConnectedToPrimaryHost()) {
							// Parse out the bdSeq number
							payload = new SparkplugBPayloadDecoder().buildFromByteArray(message.getPayload());
							// SparkplugUtils.decodePayload(message.getPayload());
							long incomingBdSeq = SparkplugUtil.getBdSequenceNumber(payload);
							try {
								if (birthBdSeq == incomingBdSeq) {
									// This is an LWT - but we're online and the bdSeq number matched - correct the
									// error by treating it as a rebirth
									logger.info("Got unexpected LWT for {} - publishing BIRTH sequence",
											EDGE_NODE_DESCRIPTOR);
									edgeClient.handleRebirthRequest(true);
								}
							} catch (Exception e) {
								logger.warn("Got unexpected LWT but failed to publish a new BIRTH sequence for {}",
										EDGE_NODE_DESCRIPTOR);
							}
						} else {
							logger.debug("Got unexpected LWT but not connected to primary host - ignoring");
						}
					} else {
						logger.debug("Got expected LWT for {}", EDGE_NODE_DESCRIPTOR);
					}

					return;
				}
			} catch (Exception e) {
				logger.error("Failed to handle NDEATH when connected on {}", topic, e);
				return;
			}

			if (!MessageType.NCMD.equals(topic.getType()) && !MessageType.DCMD.equals(topic.getType())) {
				logger.debug("Ignoring unexpected incoming Sparkplug message of type {}", topic.getType());
				return;
			}

			try {
				logger.debug("Decoding Sparkplug Payload");
				PayloadDecoder<SparkplugBPayload> decoder = new SparkplugBPayloadDecoder();
				payload = decoder.buildFromByteArray(message.getPayload());
				logger.debug("Message Timestamp: {}", payload.getTimestamp());
			} catch (Exception e) {
				logger.error("Failed to parse message - not acting on it", e);
				return;
			}

			if (MessageType.NCMD.equals(topic.getType())) {
				try {
					final List<Metric> receivedMetrics = payload.getMetrics();
					final List<Metric> responseMetrics = new ArrayList<>();
					if (receivedMetrics != null && !receivedMetrics.isEmpty()) {
						// Prep the payload
						Date now = new Date();
						SparkplugBPayloadMapBuilder payloadBuilder = new SparkplugBPayloadMapBuilder();
						payloadBuilder.setTimestamp(now);

						// Add the metrics
						for (Metric metric : receivedMetrics) {
							String name = metric.getName();

							logger.debug("Node Metric Name: {}", name);
							Object value = metric.getValue();
							logger.debug("Metric: {} :: {} :: {}", name, value, metric.getDataType());
							if (SparkplugMeta.METRIC_NODE_REBIRTH.equals(name) && value.equals(true)) {
								edgeClient.handleRebirthRequest(true);
							} else {
								Metric writtenMetric = dataSimulator.handleMetricWrite(EDGE_NODE_DESCRIPTOR, metric);
								if (writtenMetric != null) {
									responseMetrics.add(writtenMetric);
								}
							}
						}

						if (!responseMetrics.isEmpty()) {
							// Publish the response NDATA message
							logger.debug("Publishing NDATA based on NCMD message for {}", EDGE_NODE_DESCRIPTOR);
							payloadBuilder.addMetrics(responseMetrics);
							edgeClient.publishNodeData(payloadBuilder.createPayload());
						} else {
							logger.warn("Received NCMD with no valid metrics to write for {}", EDGE_NODE_DESCRIPTOR);
						}
					}
				} catch (Exception e) {
					logger.error("Error parsing NCMD", e);
				}
			} else if (MessageType.DCMD.equals(topic.getType())) {
				try {
					final List<Metric> receivedMetrics = payload.getMetrics();
					final List<Metric> responseMetrics = new ArrayList<>();
					if (receivedMetrics != null && !receivedMetrics.isEmpty()) {
						// Prep the payload
						Date now = new Date();
						SparkplugBPayloadMapBuilder payloadBuilder = new SparkplugBPayloadMapBuilder();
						payloadBuilder.setTimestamp(now);

						// Add the metrics
						for (Metric metric : receivedMetrics) {
							String name = metric.getName();

							logger.debug("Device Metric Name: {}", name);
							Object value = metric.getValue();
							logger.debug("Metric: {} :: {} :: {}", name, value, metric.getDataType());
							Metric writtenMetric = dataSimulator.handleMetricWrite(
									new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, topic.getDeviceId()), metric);
							if (writtenMetric != null) {
								responseMetrics.add(writtenMetric);
							}
						}

						if (!responseMetrics.isEmpty()) {
							// Publish the response NDATA message
							logger.debug("Publishing DDATA based on DCMD message for {}/{}", EDGE_NODE_DESCRIPTOR,
									topic.getDeviceId());
							payloadBuilder.addMetrics(responseMetrics);
							edgeClient.publishDeviceData(topic.getDeviceId(), payloadBuilder.createPayload());
						} else {
							logger.warn("Received DCMD with no valid metrics to write for {}/{}", EDGE_NODE_DESCRIPTOR,
									topic.getDeviceId());
						}
					}
				} catch (Throwable t) {
					logger.error("Error parsing DCMD", t);
				}
			}
		}
	}

	// ClientCallback API
	@Override
	public void connectionLost(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			Throwable cause) {
		logger.info("{}: ClientCallback connectionLost", clientId);
	}

	// ClientCallback API
	@Override
	public void connectComplete(boolean reconnect, MqttServerName mqttServerName, MqttServerUrl mqttServerUrl,
			MqttClientId clientId) {
		logger.info("{}: ClientCallback connectComplete", clientId);
	}

	/*
	 * Used to add the death sequence number
	 */
	private SparkplugBPayload addDeathSeqNum(SparkplugBPayload payload) {
		synchronized (clientLock) {
			if (payload == null) {
				payload = new SparkplugBPayloadBuilder().createPayload();
			}
			if (deathBdSeq == 256) {
				deathBdSeq = 0;
			}
			logger.trace("Death bdSeq(before) = {}", deathBdSeq);
			try {
				logger.trace("Set bdSeq number in NDEATH to {}", deathBdSeq);
				payload.addMetric(new MetricBuilder("bdSeq", MetricDataType.Int64, deathBdSeq).createMetric());

				// Increment sequence numbers in preparation for the next new connect
				birthBdSeq = deathBdSeq;
				deathBdSeq++;
			} catch (SparkplugInvalidTypeException e) {
				logger.error("Failed to create death payload", e);
				return null;
			}
			logger.trace("Death bdSeq(after) = {}", deathBdSeq);
			return payload;
		}
	}

	/*
	 * Used to add the birth sequence number
	 */
	private SparkplugBPayloadMap addBirthSeqNum(SparkplugBPayloadMap nBirthPayload) {
		synchronized (clientLock) {
			if (nBirthPayload == null) {
				nBirthPayload = new SparkplugBPayloadMapBuilder().createPayload();
			}
			logger.trace("Birth bdSeq(before) = {}", birthBdSeq);
			try {
				logger.trace("Set bdSeq number in NBIRTH to {}", birthBdSeq);
				nBirthPayload.addMetric(new MetricBuilder("bdSeq", MetricDataType.Int64, birthBdSeq).createMetric());
			} catch (SparkplugInvalidTypeException e) {
				logger.error("Failed to create birth payload", e);
				return null;
			}
			logger.trace("Birth bdSeq(after) = {}", birthBdSeq);
			return nBirthPayload;
		}
	}
}
