/********************************************************************************
 * Copyright (c) 2022 Cirrus Link Solutions and others
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
import org.eclipse.tahu.message.DefaultBdSeqManager;
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
import org.eclipse.tahu.message.model.StatePayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.model.MqttServerDefinition;
import org.eclipse.tahu.mqtt.ClientCallback;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;
import org.eclipse.tahu.util.SparkplugUtil;
import org.eclipse.tahu.util.TopicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SparkplugEdgeNode implements Runnable, MetricHandler, ClientCallback, CommandCallback {

	private static Logger logger = LoggerFactory.getLogger(SparkplugEdgeNode.class.getName());

	private static final String COMMAND_LISTENER_DIRECTORY = "/tmp/commands";
	private static final long COMMAND_LISTENER_POLL_RATE = 50L;

	private static final String GROUP_ID = "G2";
	private static final String EDGE_NODE_ID = "E2";
	private static final EdgeNodeDescriptor EDGE_NODE_DESCRIPTOR = new EdgeNodeDescriptor(GROUP_ID, EDGE_NODE_ID);
	private static final List<String> DEVICE_IDS = Arrays.asList("D2");
	private static final List<DeviceDescriptor> DEVICE_DESCRIPTORS =
			Arrays.asList(new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, "D2"));
	private static final String PRIMARY_HOST_ID = "IamHost";
	private static final boolean USE_ALIASES = false;
	private static final Long REBIRTH_DEBOUNCE_DELAY = 5000L;

	private static final MqttServerName MQTT_SERVER_NAME_1 = new MqttServerName("Mqtt Server One");
	private static final String MQTT_CLIENT_ID_1 = "Sparkplug-Tahu-Compatible-Impl-One";
	private static final MqttServerUrl MQTT_SERVER_URL_1 = MqttServerUrl.getMqttServerUrlSafe("tcp://localhost:1883");
	private static final String USERNAME_1 = "admin";
	private static final String PASSWORD_1 = "changeme";
	private static final MqttServerName MQTT_SERVER_NAME_2 = new MqttServerName("Mqtt Server Two");
	private static final String MQTT_CLIENT_ID_2 = "Sparkplug-Tahu-Compatible-Impl-Two";
	private static final MqttServerUrl MQTT_SERVER_URL_2 = MqttServerUrl.getMqttServerUrlSafe("tcp://localhost:1884");
	private static final String USERNAME_2 = "admin";
	private static final String PASSWORD_2 = "changeme";
	private static final int KEEP_ALIVE_TIMEOUT = 30;
	private static final Topic NDEATH_TOPIC =
			new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX, GROUP_ID, EDGE_NODE_ID, MessageType.NDEATH);

	private static final List<MqttServerDefinition> mqttServerDefinitions = new ArrayList<>();

	private CommandListener commandListener;

	/*
	 * Next Birth BD sequence number - same as last deathBdSeq
	 */
	private long birthBdSeq;

	/*
	 * Next Death BD sequence number
	 */
	private long deathBdSeq;

	private final DataSimulator dataSimulator =
			new RandomDataSimulator(10, new HashMap<SparkplugDescriptor, Integer>() {

				private static final long serialVersionUID = 1L;

				{
					for (DeviceDescriptor deviceDescriptor : DEVICE_DESCRIPTORS) {
						put(deviceDescriptor, 50);
					}
				}
			});

	/*
	 * Lock for manipulating the sequence number
	 */
	private Object clientLock = new Object();

	public static void main(String[] arg) {
		try {
			mqttServerDefinitions
					.add(new MqttServerDefinition(MQTT_SERVER_NAME_1, new MqttClientId(MQTT_CLIENT_ID_1, false),
							MQTT_SERVER_URL_1, USERNAME_1, PASSWORD_1, KEEP_ALIVE_TIMEOUT, NDEATH_TOPIC));
//			mqttServerDefinitions
//					.add(new MqttServerDefinition(MQTT_SERVER_NAME_2, new MqttClientId(MQTT_CLIENT_ID_2, false),
//							MQTT_SERVER_URL_2, USERNAME_2, PASSWORD_2, KEEP_ALIVE_TIMEOUT, NDEATH_TOPIC));

			System.out.println("Starting the Sparkplug Edge Node");
			System.out.println("\tGroup ID: " + GROUP_ID);
			System.out.println("\tEdge Node ID: " + EDGE_NODE_ID);
			System.out.println("\tDevice IDs: " + DEVICE_IDS);
			System.out.println("\tPrimary Host ID: " + PRIMARY_HOST_ID);
			System.out.println("\tUsing Aliases: " + USE_ALIASES);
			System.out.println("\tRebirth Debounce Delay: " + REBIRTH_DEBOUNCE_DELAY);

			for (MqttServerDefinition mqttServerDefinition : mqttServerDefinitions) {
				System.out.println("\tMQTT Server Name: " + mqttServerDefinition.getMqttServerName());
				System.out.println("\tMQTT Client ID: " + mqttServerDefinition.getMqttClientId());
				System.out.println("\tMQTT Server URL: " + mqttServerDefinition.getMqttServerUrl());
				System.out.println("\tUsername: " + mqttServerDefinition.getUsername());
				System.out.println("\tPassword: ********");
				System.out.println("\tKeep Alive Timeout: " + mqttServerDefinition.getKeepAliveTimeout());
			}

			SparkplugEdgeNode sparkplugEdgeNode = new SparkplugEdgeNode();
			Thread edgeNodeThread = new Thread(sparkplugEdgeNode);
			edgeNodeThread.start();

			// Run for a while and shutdown
			Thread.sleep(360000);
			sparkplugEdgeNode.shutdown();
		} catch (Exception e) {
			logger.error("Failed to run the Edge Node", e);
		}
	}

	private EdgeClient edgeClient;
	private Thread edgeClientThread;
	private PeriodicPublisher periodicPublisher;
	private DefaultBdSeqManager defaultBdSeqManager;
	private Thread periodicPublisherThread;

	public SparkplugEdgeNode() {
		try {
			defaultBdSeqManager = new DefaultBdSeqManager("SparkplugEdgeNode");
			deathBdSeq = defaultBdSeqManager.getNextDeathBdSeqNum();
			birthBdSeq = deathBdSeq;

			edgeClient = new EdgeClient(this, EDGE_NODE_DESCRIPTOR, DEVICE_IDS, PRIMARY_HOST_ID, USE_ALIASES,
					REBIRTH_DEBOUNCE_DELAY, mqttServerDefinitions, this, null);
		} catch (Exception e) {
			logger.error("Failed to create the Sparkplug Edge Client", e);
		}
	}

	@Override
	public void run() {
		try {
			commandListener = new CommandListener(this, COMMAND_LISTENER_DIRECTORY, COMMAND_LISTENER_POLL_RATE);
			commandListener.start();

			edgeClientThread = new Thread(edgeClient);
			edgeClientThread.start();
		} catch (Exception e) {
			logger.error("Failed to start", e);
		}
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
		return new SparkplugBPayloadEncoder().getBytes(nDeathPayload, true);
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
			periodicPublisher =
					new PeriodicPublisher(5000, dataSimulator, edgeClient, EDGE_NODE_DESCRIPTOR, DEVICE_DESCRIPTORS);
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

		if (commandListener != null) {
			commandListener.shutdown();
			commandListener = null;
		}
		if (periodicPublisher != null) {
			periodicPublisher.shutdown();
			periodicPublisher = null;
		}
		if (periodicPublisherThread != null) {
			periodicPublisherThread.interrupt();
			periodicPublisherThread = null;
		}

		if (edgeClient != null) {
			edgeClient.shutdown();
			edgeClient = null;
			edgeClientThread = null;
		}
	}

	// ClientCallback API
	@Override
	public void messageArrived(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			String rawTopic, MqttMessage message) {
		logger.info("{}: ClientCallback messageArrived on topic={}", clientId, rawTopic);

		final Topic topic;
		try {
			topic = TopicUtil.parseTopic(rawTopic);
		} catch (SparkplugParsingException e) {
			logger.error("Error parsing Sparkplug topic {}", rawTopic, e);
			return;
		}

		if (rawTopic.startsWith("spBv1.0/STATE/")) {
			try {
				logger.info("Got STATE message: {} :: {}", rawTopic, new String(message.getPayload()));
				ObjectMapper mapper = new ObjectMapper();
				StatePayload statePayload = mapper.readValue(message.getPayload(), StatePayload.class);
				edgeClient.handleStateMessage(topic.getHostApplicationId(), statePayload);
			} catch (Exception e) {
				logger.error("Failed to handle STATE message with topic={} and payload={}", rawTopic,
						new String(message.getPayload()));
			}
			return;
		} else if (!SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX.equals(TopicUtil.getSplitTopic(rawTopic)[0])) {
			logger.warn("Message received on erroneous topic: {}", rawTopic);
			return;
		} else {
			// Sparkplug message!
			final SparkplugBPayload payload;

			try {
				// Handling case where the MQTT Server publishes an LWT on our behalf but we're actually online.
				if (MessageType.NDEATH.equals(topic.getType()) && topic.getGroupId().equals(GROUP_ID)
						&& topic.getEdgeNodeId().equals(EDGE_NODE_ID)) {
					if (!edgeClient.isDisconnectedOrDisconnecting()) {
						if (edgeClient.isConnectedToPrimaryHost()) {
							// Parse out the bdSeq number
							payload = new SparkplugBPayloadDecoder().buildFromByteArray(message.getPayload(), null);
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
				payload = decoder.buildFromByteArray(message.getPayload(), null);
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

	// CommandCallback API
	@Override
	public void setDeviceOffline(String deviceId) {
		edgeClient.publishDeviceDeath(deviceId);
	}

	// CommandCallback API
	@Override
	public void setDeviceOnline(String deviceId) {
		SparkplugBPayload dBirthPayload =
				dataSimulator.getDeviceBirthPayload(new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, deviceId));
		edgeClient.publishDeviceBirth(deviceId, dBirthPayload);
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
				defaultBdSeqManager.storeNextDeathBdSeqNum(deathBdSeq);
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
