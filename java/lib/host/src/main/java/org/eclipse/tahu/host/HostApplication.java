/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.host.api.HostApplicationEventHandler;
import org.eclipse.tahu.host.seq.SequenceReorderManager;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugMeta;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttOperatorDefs;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;
import org.eclipse.tahu.mqtt.RandomStartupDelay;
import org.eclipse.tahu.mqtt.TahuClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostApplication implements CommandPublisher {

	private static Logger logger = LoggerFactory.getLogger(HostApplication.class.getName());

	private static int MAX_INFLIGHT_MESSAGES = 500;

	private final String hostId;
	private final MqttClientId mqttClientId;
	private final MqttServerName mqttServerName;
	private final MqttServerUrl mqttServerUrl;
	private final String username;
	private final String password;
	private final int keepAliveTimeout;
	private final RandomStartupDelay randomStartupDelay;
	private final String stateTopic;

	private TahuClient tahuClient;
	private final TahuHostCallback tahuHostCallback;

	private int initialBdSeq;

	public HostApplication(HostApplicationEventHandler eventHandler, String hostId, MqttClientId mqttClientId,
			MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, String username, String password,
			int keepAliveTimeout, RandomStartupDelay randomStartupDelay, int initialBdSeq) {
		logger.info("Creating the Host Application");

		this.hostId = hostId;
		this.mqttClientId = mqttClientId;
		this.mqttServerName = mqttServerName;
		this.mqttServerUrl = mqttServerUrl;
		this.username = username;
		this.password = password;
		this.keepAliveTimeout = keepAliveTimeout;
		this.randomStartupDelay = randomStartupDelay;
		this.stateTopic = SparkplugMeta.SPARKPLUG_TOPIC_HOST_STATE_PREFIX + "/" + hostId;

		SequenceReorderManager sequenceReorderManager = SequenceReorderManager.getInstance();
		sequenceReorderManager.init(eventHandler, this, 5000L);
		this.tahuHostCallback = new TahuHostCallback(eventHandler, this, sequenceReorderManager);

		this.initialBdSeq = initialBdSeq;
	}

	public void start() {
		logger.debug("Starting up the MQTT Client");
		if (tahuClient == null) {
			tahuClient = new TahuClient(mqttClientId, mqttServerName, mqttServerUrl, username, password, true,
					keepAliveTimeout, tahuHostCallback, randomStartupDelay, true, stateTopic, null, true, stateTopic,
					null, MqttOperatorDefs.QOS1, true);
		}

		tahuClient.setMaxInflightMessages(MAX_INFLIGHT_MESSAGES);
		Map<MqttServerName, TahuClient> tahuClients = new HashMap<>();
		tahuClients.put(mqttServerName, tahuClient);
		tahuHostCallback.setMqttClients(tahuClients);

		try {
			tahuClient.setAutoReconnect(true);
			tahuClient.connect();

			// Subscribe to our own spBv1.0/STATE topic
			logger.debug("PrimaryHostId is set. Subscribing on {}", stateTopic);
			int grantedQos = tahuClient.subscribe(stateTopic, MqttOperatorDefs.QOS1);
			if (grantedQos != 1) {
				logger.error("Failed to subscribe to '{}'", stateTopic);
				return;
			}

			// Subscribe to the spBv1.0 namespace
			String topic = "spBv1.0/#";
			logger.debug("PrimaryHostId is set. Subscribing on {}", topic);
			grantedQos = tahuClient.subscribe(topic, MqttOperatorDefs.QOS0);
			if (grantedQos != 0) {
				logger.error("Failed to subscribe to '{}'", topic);
				return;
			}

			// Pub
		} catch (Exception e) {
			logger.error("Failed to start client {} connecting to {}", tahuClient.getClientId(),
					tahuClient.getMqttServerUrl(), e);
			return;
		}

		logger.debug("MQTT Clients Started. Connection and subscriptions not verified yet");

	}

	public void shutdown() {
		if (tahuClient != null) {
			String connectionId = new StringBuilder().append(tahuClient.getMqttServerUrl()).append(" :: ")
					.append(tahuClient.getClientId()).toString();
			try {
				// Unsubscribe
				// removeMqttClientSubscriptions(tahuClient, unsubscribe);

				// Clean up spBv1.0/STATE subscriptions
				logger.debug("Unsubscribing from {}", stateTopic);
				tahuClient.unsubscribe(stateTopic);

				// Clean up the Sparkplug subscription
				String topic = "spBv1.0/#";
				logger.debug("Unsubscribing from {}", topic);
				tahuClient.unsubscribe(topic);

				// Shut down the client after the MQTT client is disconnected to prevent RejectedExecutionExceptions
				tahuHostCallback.shutdown();

				// Shut down the MQTT client
				tahuClient.setAutoReconnect(false);
				logger.info("Attempting disconnect {}", connectionId);
				tahuClient.disconnect(0, 1, false, true);
				logger.info("Successfully disconnected {}", connectionId);

				// Set the Edge Nodes associated with this client offline
//				edgeNodeManager.setAllEdgeNodesOffline(mqttServerName);
			} catch (Exception e) {
				logger.error("Error shutting down {}", connectionId, e);
			} finally {
				tahuClient = null;
			}
		} else {
			logger.trace("Cannot shutdown null client");
		}
	}

	@Override
	public void publishCommand(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId, Topic topic,
			SparkplugBPayload payload) throws Exception {
		if (tahuClient != null && tahuClient.isConnected()) {
			SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
			byte[] bytes = encoder.getBytes(payload);
			tahuClient.publish(topic.toString(), bytes, MqttOperatorDefs.QOS0, false);
		} else {
			throw new TahuException(TahuErrorCode.INITIALIZATION_ERROR,
					"The Tahu Client is not connected - not publishing command on topic=" + topic);
		}
	}
}
