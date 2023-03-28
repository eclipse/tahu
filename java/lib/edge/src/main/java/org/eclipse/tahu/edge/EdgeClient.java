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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.edge.api.MetricHandler;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap.SparkplugBPayloadMapBuilder;
import org.eclipse.tahu.message.model.SparkplugMeta;
import org.eclipse.tahu.message.model.StatePayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.model.MetricMap;
import org.eclipse.tahu.model.MqttServerDefinition;
import org.eclipse.tahu.mqtt.ClientCallback;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttOperatorDefs;
import org.eclipse.tahu.mqtt.RandomStartupDelay;
import org.eclipse.tahu.mqtt.TahuClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeClient implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(EdgeClient.class.getName());

	private final List<MqttServerDefinition> mqttServerDefinitions;
	private final ClientCallback callback;

	private final MetricHandler metricHandler;
	private final EdgeNodeDescriptor edgeNodeDescriptor;
	private final Map<String, Boolean> deviceStatusMap;
	private final String primaryHostId;
	private final MetricMap metricMap;
	private final long rebirthDebounceDelay; // The user specified Rebirth Debounce Delay
	private final RandomStartupDelay randomStartupDelay;

	private TahuClient tahuClient;

	private final Object clientLock = new Object();

	private int seq;

	private int currentMqttClientIndex;

	// Tracking variables
	private volatile boolean stayRunning;
	private boolean connectedToPrimaryHost; // Whether or not this client is connected to Primary Host ID
	private Long lastStatePayloadTimestamp;
	private Timer primaryHostIdResponseTimer; // The Primary Host ID response timer
	private Timer rebirthDelayTimer; // A Timer used to prevent multiple rebirth requests while the timer is running

	public EdgeClient(MetricHandler metricHandler, EdgeNodeDescriptor edgeNodeDescriptor, List<String> deviceIds,
			String primaryHostId, boolean useAliases, Long rebirthDebounceDelay,
			List<MqttServerDefinition> mqttServerDefinitions, ClientCallback callback,
			RandomStartupDelay randomStartupDelay) {

		this.mqttServerDefinitions = mqttServerDefinitions;
		this.callback = callback;

		this.metricHandler = metricHandler;
		this.edgeNodeDescriptor = edgeNodeDescriptor;
		this.deviceStatusMap = new ConcurrentHashMap<>();
		if (deviceIds != null) {
			for (String deviceId : deviceIds) {
				deviceStatusMap.put(deviceId, new Boolean(false));
			}
		}
		this.primaryHostId = primaryHostId;
		this.metricMap = useAliases ? new MetricMap() : null;
		this.rebirthDebounceDelay = rebirthDebounceDelay;
		this.randomStartupDelay = randomStartupDelay;

		stayRunning = true;
		connectedToPrimaryHost = false;
		currentMqttClientIndex = -1;
	}

	public void shutdown() {
		disconnect(true);
		stayRunning = false;
		connectedToPrimaryHost = false;
	}

	public boolean isDisconnectedOrDisconnecting() {
		return tahuClient.isDisconnectInProgress() || !tahuClient.isConnected();
	}

	public boolean isConnected() {
		if (tahuClient == null || !tahuClient.isConnected()) {
			return false;
		} else {
			return true;
		}
	}

	public boolean isConnectedToPrimaryHost() {
		return connectedToPrimaryHost;
	}

	public void disconnect(boolean publishLwt) {
		synchronized (clientLock) {
			logger.debug("{} Attempting to disconnect from target server",
					mqttServerDefinitions.get(currentMqttClientIndex).getMqttClientId());

			// Cancel the primaryHostId if it is running
			if (primaryHostIdResponseTimer != null) {
				logger.debug("Cancelling the primary host ID timer");
				primaryHostIdResponseTimer.cancel();
				primaryHostIdResponseTimer = null;
			}
			connectedToPrimaryHost = false;

			// Attempt to close and clear the client if it is not already null
			if (tahuClient != null) {
				String connectionId = new StringBuilder().append(tahuClient.getMqttServerUrl()).append(" :: ")
						.append(tahuClient.getClientId()).toString();
				logger.info("Attempting disconnect {}", connectionId);
				try {
					if (publishLwt) {
						for (String deviceId : deviceStatusMap.keySet()) {
							// Publish all of the DDEATHs since we're shutting down cleanly
							publishDeviceDeath(deviceId);
						}

						tahuClient.disconnect(50, 50, true, true, false);
					} else {
						tahuClient.disconnect(0, 1, false, false, false);
					}
					logger.info("Successfully disconnected {}", connectionId);
				} catch (Throwable t) {
					logger.error("Error while attempting to close client: {}", connectionId, t);
				}
			}
		}
	}

	public void publishNodeBirth(SparkplugBPayloadMap payload) throws SparkplugInvalidTypeException {
		if (metricMap != null) {
			// Aliasing is enabled so reinitialize the alias map and add the new NBIRTH metrics
			metricMap.clear();
			for (Metric metric : payload.getMetrics()) {
				metric.setAlias(metricMap.addGeneratedAlias(metric.getName(), metric.getDataType()));
			}
		}

		// Ensure the 'Node Control/Rebirth' metric is present
		if (payload.getMetric("Node Control/Rebirth") == null) {
			payload.addMetric(new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, false).createMetric());
		}

		publishSparkplugMessage(
				new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX, edgeNodeDescriptor, MessageType.NBIRTH), payload, 0,
				false);
	}

	public void publishNodeData(SparkplugBPayload payload) {
		if (connectedToPrimaryHost) {
			if (metricMap != null) {
				// Aliasing is enabled so replace metric names with aliases
				for (Metric metric : payload.getMetrics()) {
					metric.setAlias(metricMap.getAlias(metric.getName()));
					metric.setName(null);
				}
			}

			publishSparkplugMessage(
					new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX, edgeNodeDescriptor, MessageType.NDATA), payload,
					0, false);
		}
	}

	public void publishDeviceBirth(String deviceId, SparkplugBPayload payload) {
		if (metricMap != null) {
			// Aliasing is enabled so add the new DBIRTH metrics
			for (Metric metric : payload.getMetrics()) {
				metric.setAlias(metricMap.addGeneratedAlias(metric.getName(), metric.getDataType()));
			}
		}

		publishSparkplugMessage(new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX,
				new DeviceDescriptor(edgeNodeDescriptor, deviceId), MessageType.DBIRTH), payload, 0, false);
		deviceStatusMap.put(deviceId, new Boolean(true));
	}

	public void publishDeviceData(String deviceId, SparkplugBPayload payload) {
		if (connectedToPrimaryHost) {
			if (metricMap != null && deviceStatusMap.get(deviceId) != null
					&& deviceStatusMap.get(deviceId).booleanValue()) {
				// Aliasing is enabled so replace metric names with aliases
				for (Metric metric : payload.getMetrics()) {
					metric.setAlias(metricMap.getAlias(metric.getName()));
					metric.setName(null);
				}
			}

			publishSparkplugMessage(new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX,
					new DeviceDescriptor(edgeNodeDescriptor, deviceId), MessageType.DDATA), payload, 0, false);
		}
	}

	public void publishDeviceDeath(String deviceId) {
		SparkplugBPayloadMapBuilder payloadBuilder = new SparkplugBPayloadMapBuilder();
		payloadBuilder.setTimestamp(new Date());
		publishSparkplugMessage(new Topic(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX,
				new DeviceDescriptor(edgeNodeDescriptor, deviceId), MessageType.DDEATH), payloadBuilder.createPayload(),
				0, false);
		deviceStatusMap.put(deviceId, new Boolean(false));
	}

	private void publishSparkplugMessage(Topic topic, SparkplugBPayload payload, int qos, boolean retained) {
		synchronized (clientLock) {
			try {
				payload.setSeq(getNextSeqNum());
				if (topic.isType(MessageType.DCMD) || topic.isType(MessageType.DDATA) || topic.isType(MessageType.NCMD)
						|| topic.isType(MessageType.NDATA)) {
					tahuClient.publish(topic.toString(), new SparkplugBPayloadEncoder().getBytes(payload, true), qos,
							retained);
				} else {
					tahuClient.publish(topic.toString(), new SparkplugBPayloadEncoder().getBytes(payload, false), qos,
							retained);
				}
			} catch (Exception e) {
				logger.error("Failed to publish message on topic={}", topic, e);
			}
		}
	}

	public long getNextSeqNum() {
		synchronized (clientLock) {
			if (seq == 256) {
				seq = 0;
			}
			logger.trace("INC: SEQ number is: {}", seq);
			return seq++;
		}
	}

	// Runnable API
	@Override
	/**
	 * The main runtime thread that handles the life-cycle of MQTT sessions for Transmission Edge Nodes
	 */
	public void run() {
		logger.info("Running EdgeClient: {}", edgeNodeDescriptor);
		while (stayRunning) {
			synchronized (clientLock) {
				try {
					boolean tryToConnect = false;
					boolean transitionToOnline = false;
					if (tahuClient == null || !tahuClient.isConnected()) {
						if (!stayRunning) {
							return;
						} else {
							logger.warn("{} Not connected - attempting connect with isStayRunning={}",
									edgeNodeDescriptor, stayRunning);
							tryToConnect = true;
						}
					}

					if (stayRunning && tryToConnect) {
						boolean connectedToServer = connectToTargetServer();

						// Subscribe to our data feeds... and publish required BIRTH Certs
						if (connectedToServer) {
							try {
								// Set transitionToOnline true
								transitionToOnline = true;

								// Subscribe to all of the topics
								List<String> subTopics = new ArrayList<>();
								List<Integer> subQos = new ArrayList<>();

								// Subscribe to NCMD messages
								subTopics.add(
										SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + edgeNodeDescriptor.getGroupId()
												+ "/NCMD/" + edgeNodeDescriptor.getEdgeNodeId());
								subQos.add(1);

								// Subscribe to DCMDs
								if (deviceStatusMap != null && !deviceStatusMap.isEmpty()) {
									for (String deviceId : deviceStatusMap.keySet()) {
										subTopics.add(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/"
												+ edgeNodeDescriptor.getGroupId() + "/DCMD/"
												+ edgeNodeDescriptor.getEdgeNodeId() + "/" + deviceId);
										subQos.add(1);
									}
								}

								// Subscribe to our own LWT
								subTopics.add(
										SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + edgeNodeDescriptor.getGroupId()
												+ "/NDEATH/" + edgeNodeDescriptor.getEdgeNodeId());
								subQos.add(1);

								if (primaryHostId != null && !primaryHostId.isEmpty()) {
									subTopics
											.add(SparkplugMeta.SPARKPLUG_TOPIC_HOST_STATE_PREFIX + "/" + primaryHostId);
									subQos.add(1);
								}

								int[] grantedQos = tahuClient.subscribe(subTopics.toArray(new String[0]),
										subQos.stream().mapToInt(i -> i).toArray());
								if (grantedQos == null || grantedQos.length == 0) {
									logger.error("Failed to subscribe to: {}", subTopics);
									transitionToOnline = false;
									disconnect(true);
								}
							} catch (TahuException e) {
								logger.error("Failed to subscribe to TARGET elements", e);
								connectedToServer = false;
								transitionToOnline = false;
							}
						} else {
							disconnect(true);
						}
					}

					if (transitionToOnline) {
						// In a transition to an MQTT session, publish the NBIRTH and DBIRTH messages.
						transitionToOnline = false;

						// Check if the server type is NOT JSON and we have specified a primary host ID
						if (primaryHostId != null && !primaryHostId.isEmpty()) {
							// Set up the primary host ID Timer
							logger.info("Waiting for primary host {} to be online", primaryHostId);
							connectedToPrimaryHost = false;
							// Start a timer to run while we wait for a response;
							if (primaryHostIdResponseTimer != null) {
								primaryHostIdResponseTimer.cancel();
								primaryHostIdResponseTimer = null;
							}
							primaryHostIdResponseTimer = new Timer(
									String.format("PrimaryHostIdResponseTimer-%s", edgeNodeDescriptor.toString()));
							primaryHostIdResponseTimer.schedule(new PrimaryHostIdResponseTask(), 30000);

							// Subscribe to the STATE topic for primary host ID notifications
							String subHostTopic = SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + primaryHostId;
							int grantedQos = tahuClient.subscribe(subHostTopic, MqttOperatorDefs.QOS1);
							if (grantedQos != 1) {
								logger.error("Failed to subscribe to '{}'", subHostTopic);
								// Cancel the timer and disconnect
								if (primaryHostIdResponseTimer != null) {
									primaryHostIdResponseTimer.cancel();
									primaryHostIdResponseTimer = null;
								}
								disconnect(true);
							}
						} else {
							handleOnlineTransition("MAIN THREAD");
						}
					}
				} catch (Exception e) {
					logger.error("Stay Running Exception", e);
				}
			}
		}
	}

	/*
	 * Connects to an MQTT Server
	 *
	 * @return true if the attempt succeeded and client is not stale
	 */
	private boolean connectToTargetServer() {
		synchronized (clientLock) {
			if (tahuClient != null && tahuClient.isConnected()) {
				logger.debug("Not connecting to server, client is already connected");
				return false;
			}

			MqttClientId mqttClientId = null;
			try {
				Topic deathTopic = metricHandler.getDeathTopic();
				byte[] deathPayloadBytes = null;
				try {
					deathPayloadBytes = metricHandler.getDeathPayloadBytes();
				} catch (TahuException te) {
					logger.error("Failed to get the NDEATH message deathTopic={} - disconnecting and BAILING",
							deathTopic);
					stayRunning = false;
					disconnect(true);
					return false;
				}
				if (deathTopic == null || deathPayloadBytes == null) {
					logger.error("Failed to get the NDEATH message deathTopic={} and deathPayloadBytes={}", deathTopic,
							deathPayloadBytes);
					return false;
				}

				currentMqttClientIndex++;
				if (currentMqttClientIndex >= mqttServerDefinitions.size()) {
					currentMqttClientIndex = 0;
				}
				MqttServerDefinition mqttServerDefinition = mqttServerDefinitions.get(currentMqttClientIndex);
				mqttClientId = mqttServerDefinition.getMqttClientId();
				tahuClient = new TahuClient(mqttClientId, mqttServerDefinition.getMqttServerName(),
						mqttServerDefinition.getMqttServerUrl(), mqttServerDefinition.getUsername(),
						mqttServerDefinition.getPassword(), true, mqttServerDefinition.getKeepAliveTimeout(), callback,
						randomStartupDelay, false, null, null, false, deathTopic.toString(), deathPayloadBytes, 1,
						false);
				tahuClient.setTrackFirstConnection(true);
				tahuClient.setAutoReconnect(false);

				logger.info("{} Attempting to connect", mqttClientId);
				tahuClient.connect();

				// Loop for 1.5 times the keep-alive timeout + randomStartupDelay + rebirthDebounceDelay, waiting for
				// the client to connect or finish attempting to connect.
				int totalTimeout =
						(int) (((int) tahuClient.getKeepAlive() * 1.5) + ((int) rebirthDebounceDelay / 1000));
				logger.debug("Total timeout to connect is {} seconds", totalTimeout);
				for (int i = 0; i < totalTimeout; i++) {
					if (tahuClient.isAttemptingConnect()) {
						logger.info("{} is attempting to connect", mqttClientId);
					} else {
						logger.info("{} is not attempting to connect", mqttClientId);
					}

					if (!stayRunning) {
						// Attempt to disconnect from the target server
						logger.debug("{} Shutting down", mqttServerDefinition.getMqttClientId());
						disconnect(true);
						return false;
					} else if (tahuClient.isAttemptingConnect()) {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							logger.error("Error occured while sleeping", e);
						}
					} else if (tahuClient.isConnected()) {
						logger.info("{} Connected to the MQTT Server", mqttClientId);
						return true;
					} else {
						logger.info("{} No longer attempting to connect", mqttClientId);
						break;
					}
				}

				// Attempt to disconnect from the target server
				logger.error("{} Failed to achieve connected state", mqttClientId);
				disconnect(true);

				// Return false to indicate a failed connect attempt
				return false;
			} catch (Throwable t) {
				logger.error("{} Error while attempting to connect to target server for {}", mqttClientId,
						edgeNodeDescriptor, t);
				logger.info("\ttahuClient: {}", tahuClient);

				// Attempt to disconnect from the target server
				disconnect(true);

				// Return false to indicate a failed connect attempt
				return false;
			}
		}
	}

	/*
	 * Handles the transition to online
	 */
	private void handleOnlineTransition(String source) {
		if (!stayRunning) {
			logger.debug("EdgeClient is shutting down - not publishing BIRTH messages");
			disconnect(true);
			return;
		} else {
			logger.info("[{}] Handling transition to online", source);
		}

		try {
			logger.debug("Publishing BIRTH for {}", edgeNodeDescriptor);
			seq = 0;
			metricHandler.publishBirthSequence();
		} catch (Exception e) {
			logger.error("Failed to publish birth - BAILING", e);
			stayRunning = false;
			disconnect(true);
			return;
		}

		// This should happen after the birth sequence so DATA messages can't be published before the BIRTHs
		connectedToPrimaryHost = true;
	}

	/**
	 * Handles state messages received by the Edge Node.
	 *
	 * @param primaryHostId the primary host ID
	 * @param state the state
	 */
	public void handleStateMessage(String primaryHostId, StatePayload statePayload) {
		synchronized (clientLock) {
			if (this.primaryHostId != null && this.primaryHostId.equals(primaryHostId)) {
				Long payloadTimestamp = statePayload.getTimestamp();
				if (lastStatePayloadTimestamp != null && payloadTimestamp.compareTo(lastStatePayloadTimestamp) < 0) {
					logger.info("Reveived a stale STATE message - ignoring hostId={} and payload={}", primaryHostId,
							statePayload);
					return;
				} else {
					lastStatePayloadTimestamp = payloadTimestamp;
				}

				if (statePayload.isOnline() && !connectedToPrimaryHost) {
					logger.info("Critical/Primary app is online - cancelling disconnect timer");
					if (primaryHostIdResponseTimer != null) {
						primaryHostIdResponseTimer.cancel();
						primaryHostIdResponseTimer = null;
					}
					handleOnlineTransition("STATE CHANGE");
				} else if (!statePayload.isOnline()) {
					logger.error("Critical/Primary app went offline - disconnecting from this server");
					// Check if currently connected to primary host
					if (connectedToPrimaryHost) {
						connectedToPrimaryHost = false;
						disconnect(true);
					} else {
						// Disconnect cleanly and don't publish LWT
						disconnect(false);
					}
				}
			}
		}
	}

	/**
	 * Processes an Edge Node "Rebirth" request.
	 */
	public void handleRebirthRequest(boolean isRebirth) {
		synchronized (clientLock) {
			if (tahuClient == null) {
				logger.warn("Not processing {} request, client is null", isRebirth ? "Rebirth" : "Birth");
			} else if (!stayRunning) {
				logger.warn("Not processing {} request, client is shutting down", isRebirth ? "Rebirth" : "Birth");
			} else if (rebirthDelayTimer == null) {
				logger.info("Processing {} request", isRebirth ? "Rebirth" : "Birth");
				seq = 0;
				metricHandler.publishBirthSequence();
				long randomDelay = randomStartupDelay != null ? randomStartupDelay.getRandomDelay() : 0L;
				rebirthDelayTimer = new Timer(String.format("RebirthDelayTimer-%s", edgeNodeDescriptor.toString()));
				logger.debug("Setting RebirthDelayTimer to {}ms", randomDelay + rebirthDebounceDelay);
				rebirthDelayTimer.schedule(new RebirthDelayTask(), randomDelay + rebirthDebounceDelay);
			} else {
				logger.info("Rebirth request but just issued a rebirth - ignoring");
			}
		}
	}

	private class PrimaryHostIdResponseTask extends TimerTask {
		public void run() {
			logger.error("Failed to validate the Primary Host is online");
			disconnect(true);
		}
	}

	private class RebirthDelayTask extends TimerTask {
		public void run() {
			rebirthDelayTimer.cancel();
			rebirthDelayTimer = null;
		}
	}
}
