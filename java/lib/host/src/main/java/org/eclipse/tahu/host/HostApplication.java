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

package org.eclipse.tahu.host;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.host.api.HostApplicationEventHandler;
import org.eclipse.tahu.host.seq.SequenceReorderManager;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugMeta;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.model.MqttServerDefinition;
import org.eclipse.tahu.mqtt.MqttOperatorDefs;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.RandomStartupDelay;
import org.eclipse.tahu.mqtt.TahuClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HostApplication implements CommandPublisher {

	private static Logger logger = LoggerFactory.getLogger(HostApplication.class.getName());

	private static int MAX_INFLIGHT_MESSAGES = 500;

	private final String hostId;
	private final RandomStartupDelay randomStartupDelay;
	private final String stateTopic;

	private final TahuHostCallback tahuHostCallback;

	private final List<MqttServerDefinition> mqttServerDefinitions;
	private final Map<MqttServerName, TahuClient> tahuClients = new HashMap<>();

	public HostApplication(HostApplicationEventHandler eventHandler, String hostId,
			List<MqttServerDefinition> mqttServerDefinitions, RandomStartupDelay randomStartupDelay) {
		logger.info("Creating the Host Application");

		this.hostId = hostId;
		this.mqttServerDefinitions = mqttServerDefinitions;
		this.randomStartupDelay = randomStartupDelay;
		this.stateTopic = SparkplugMeta.SPARKPLUG_TOPIC_HOST_STATE_PREFIX + "/" + hostId;

		SequenceReorderManager sequenceReorderManager = SequenceReorderManager.getInstance();
		sequenceReorderManager.init(eventHandler, this, 5000L);
		this.tahuHostCallback = new TahuHostCallback(eventHandler, this, sequenceReorderManager);
	}

	public void start() {
		for (MqttServerDefinition mqttServerDefinition : mqttServerDefinitions) {
			logger.debug("Starting up the MQTT Client to {}", mqttServerDefinition.getMqttServerName());
			TahuClient tahuClient = tahuClients.get(mqttServerDefinition.getMqttServerName());
			if (tahuClient == null) {
				tahuClient = new TahuClient(mqttServerDefinition.getMqttClientId(),
						mqttServerDefinition.getMqttServerName(), mqttServerDefinition.getMqttServerUrl(),
						mqttServerDefinition.getUsername(), mqttServerDefinition.getPassword(), true,
						mqttServerDefinition.getKeepAliveTimeout(), tahuHostCallback, randomStartupDelay, true,
						stateTopic, null, true, stateTopic, null, MqttOperatorDefs.QOS1, true);
			}

			tahuClient.setMaxInflightMessages(MAX_INFLIGHT_MESSAGES);
			tahuClients.put(mqttServerDefinition.getMqttServerName(), tahuClient);
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
		}

		logger.debug("MQTT Clients Started. Connection and subscriptions not verified yet");
	}

	public void shutdown() {
		for (TahuClient tahuClient : tahuClients.values()) {
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
					tahuClient.disconnect(100, 100, true, true);
					logger.info("Successfully disconnected {}", connectionId);

					// Set the Edge Nodes associated with this client offline
//					edgeNodeManager.setAllEdgeNodesOffline(mqttServerName);
				} catch (Exception e) {
					logger.error("Error shutting down {}", connectionId, e);
				} finally {
					tahuClient = null;
				}
			} else {
				logger.trace("Cannot shutdown null client");
			}
		}
	}

	@Override
	public void publishCommand(Topic topic, SparkplugBPayload payload) throws Exception {
		for (MqttServerName mqttServerName : tahuClients.keySet()) {
			publishCommand(mqttServerName, topic, payload);
		}
	}

	@Override
	public void publishCommand(MqttServerName mqttServerName, Topic topic, SparkplugBPayload payload) throws Exception {
		TahuClient tahuClient = tahuClients.get(mqttServerName);
		if (tahuClient != null && tahuClient.isConnected()) {
			SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
			byte[] bytes = encoder.getBytes(payload, true);
			tahuClient.publish(topic.toString(), bytes, MqttOperatorDefs.QOS0, false);
		} else {
			throw new TahuException(TahuErrorCode.INITIALIZATION_ERROR,
					"The Tahu Client is not connected - not publishing command on topic=" + topic);
		}
	}
}
