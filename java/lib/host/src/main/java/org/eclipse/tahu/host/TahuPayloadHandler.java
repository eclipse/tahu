/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.host.alias.HostApplicationAliasMap;
import org.eclipse.tahu.host.api.HostApplicationEventHandler;
import org.eclipse.tahu.host.manager.EdgeNodeManager;
import org.eclipse.tahu.host.manager.SparkplugDevice;
import org.eclipse.tahu.host.manager.SparkplugEdgeNode;
import org.eclipse.tahu.host.model.MessageContext;
import org.eclipse.tahu.message.PayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.util.SparkplugUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TahuPayloadHandler {

	private static Logger logger = LoggerFactory.getLogger(TahuPayloadHandler.class.getName());

	private static Map<EdgeNodeDescriptor, Timer> rebirthTimers = new ConcurrentHashMap<>();

	private final HostApplicationEventHandler eventHandler;

	private final CommandPublisher commandPublisher;

	public TahuPayloadHandler(HostApplicationEventHandler eventHandler, CommandPublisher commandPublisher) {
		this.eventHandler = eventHandler;
		this.commandPublisher = commandPublisher;
	}

	public void handlePayload(String topicString, String[] splitTopic, MqttMessage message,
			MqttServerName mqttServerName, MqttClientId hostAppMqttClientId) {
		logger.trace("Handling payload on {}", topicString);

		Topic topic = null;
		try {
			if (splitTopic.length == 4) {
				topic = new Topic(splitTopic[0], splitTopic[1], splitTopic[3], MessageType.valueOf(splitTopic[2]));
			} else if (splitTopic.length == 5) {
				topic = new Topic(splitTopic[0], splitTopic[1], splitTopic[3], splitTopic[4],
						MessageType.valueOf(splitTopic[2]));
			} else {
				logger.error("Failed to handle the topic '{}'", topicString);
				return;
			}
		} catch (Exception e) {
			logger.error("Error parsing topic", e);
			return;
		}
		MessageType type = topic.getType();

		SparkplugBPayload payload = null;
		try {
			// Parse the payload
			PayloadDecoder<SparkplugBPayload> decoder = new SparkplugBPayloadDecoder();
			payload = decoder.buildFromByteArray(message.getPayload());
			logger.trace("On topic={}: Incoming payload: {}", topic, payload);
		} catch (Exception e) {
			logger.error("Failed to decode the payload", e);
			return;
		}

		if (type.isCommand()) {
			// This was an outbound command - ignore it
			logger.debug("Ignoring outbound command: {}", topicString);
			return;
		}

		// Extract the sequence number unless it is a node death certificate
		Long seqNum = null;
		if (!type.equals(MessageType.NDEATH)) {
			if (payload == null || payload.getSeq() == null) {
				logger.error("Invalid payload with topic={}: {}", topicString,
						payload == null ? "payload is null" : "sequence number is null");
				return;
			} else {
				seqNum = payload.getSeq();
				if (seqNum == null) {
					logger.error("Invalid payload missing sequence number: {}", topicString);
					return;
				}
			}
		}

		try {
			MessageContext messageContext = new MessageContext(mqttServerName, hostAppMqttClientId, topic,
					message.getPayload() == null ? 0 : message.getPayload().length, payload,
					seqNum == null ? -1 : seqNum);

			switch (type) {
				case NBIRTH:
					logger.info("Handling NBIRTH from {}", topic.getSparkplugDescriptor());
					handleNodeBirth(messageContext);
					break;
				case DBIRTH:
					logger.info("Handling DBIRTH from {}", topic.getSparkplugDescriptor());
					handleDeviceBirth(messageContext);
					break;
				case NDATA:
					logger.info("Handling NDATA from {}", topic.getSparkplugDescriptor());
					handleNodeData(messageContext);
					break;
				case DDATA:
					logger.info("Handling DDATA from {}", topic.getSparkplugDescriptor());
					handleDeviceData(messageContext);
					break;
//				case NDEATH:
//					logger.info("Handling NDEATH from {}", topic.getSparkplugDescriptor());
//					Long bdSeqNum = -1L;
//					try {
//						bdSeqNum = getCurrentBdSequenceNumber(payload);
//					} catch (Exception e) {
//						logger.warn("Sparkplug BD sequence number from {} is missing", getEdgeNodeDescriptor());
//					}
//					handleNodeDeath(engineMqttServerName, bdSeqNum,
//							getTransmissionVersion(new EdgeNodeDescriptor(topic.getGroupId(), topic.getEdgeNodeId())));
//					break;
//				case DDEATH:
//					logger.info("Handling NDEATH from {}", topic.getSparkplugDescriptor());
//					handleDeviceDeath(topic.getDeviceId(), messageContext);
//					break;

				default:
					logger.info("Unknown message with type={} on topic={}", type, topic);
			}
		} catch (Exception e) {
			logger.error("Failed to handle payload on topic: {} with payload={}", topic, payload, e);
			return;
		}
	}

	protected void handleNodeBirth(MessageContext messageContext) throws Exception {
		logger.debug("Processing NBIRTH from Edge Node {} with Seq# {}",
				messageContext.getTopic().getEdgeNodeDescriptor(), messageContext.getSeqNum());
		EdgeNodeDescriptor edgeNodeDescriptor = messageContext.getTopic().getEdgeNodeDescriptor();
		SparkplugEdgeNode sparkplugEdgeNode =
				EdgeNodeManager.getInstance().getSparkplugEdgeNode(messageContext.getTopic().getEdgeNodeDescriptor());
		if (sparkplugEdgeNode == null) {
			sparkplugEdgeNode = EdgeNodeManager.getInstance().addSparkplugEdgeNode(edgeNodeDescriptor,
					messageContext.getMqttServerName(), messageContext.getHostAppMqttClientId());
		}

		sparkplugEdgeNode.setOnline(true, messageContext.getPayload().getTimestamp(),
				SparkplugUtil.getBdSequenceNumber(messageContext.getPayload()), messageContext.getSeqNum());

		eventHandler.onNodeBirthArrived(edgeNodeDescriptor);
		for (Metric metric : messageContext.getPayload().getMetrics()) {
			if (metric.hasAlias()) {
				HostApplicationAliasMap.getInstance().addAlias(edgeNodeDescriptor, metric.getName(), metric.getAlias());
			}

			eventHandler.onBirthMetric(edgeNodeDescriptor, metric);
		}
		eventHandler.onNodeBirthComplete(edgeNodeDescriptor);
	}

	protected void handleDeviceBirth(MessageContext messageContext) throws Exception {
		logger.debug("Processing DBIRTH from Device {} with Seq# {}",
				messageContext.getTopic().getSparkplugDescriptor(), messageContext.getSeqNum());
		EdgeNodeDescriptor edgeNodeDescriptor = messageContext.getTopic().getEdgeNodeDescriptor();
		DeviceDescriptor deviceDescriptor = (DeviceDescriptor) messageContext.getTopic().getSparkplugDescriptor();
		SparkplugEdgeNode sparkplugEdgeNode = EdgeNodeManager.getInstance().getSparkplugEdgeNode(edgeNodeDescriptor);
		SparkplugDevice sparkplugDevice =
				EdgeNodeManager.getInstance().getSparkplugDevice(edgeNodeDescriptor, deviceDescriptor);
		if (sparkplugDevice == null) {
			sparkplugDevice = EdgeNodeManager.getInstance().addSparkplugDevice(edgeNodeDescriptor, deviceDescriptor,
					messageContext.getPayload().getTimestamp());
		}

		sparkplugEdgeNode.handleSeq(messageContext.getPayload().getSeq());

		sparkplugDevice.setOnline(true, messageContext.getPayload().getTimestamp());

		eventHandler.onDeviceBirthArrived(deviceDescriptor);
		for (Metric metric : messageContext.getPayload().getMetrics()) {
			if (metric.hasAlias()) {
				HostApplicationAliasMap.getInstance().addAlias(edgeNodeDescriptor, metric.getName(), metric.getAlias());
			}

			eventHandler.onBirthMetric(deviceDescriptor, metric);
		}
		eventHandler.onDeviceBirthComplete(deviceDescriptor);
	}

	protected void handleNodeData(MessageContext messageContext) throws Exception {
		logger.debug("Processing NDATA from Edge Node {} with Seq# {}",
				messageContext.getTopic().getEdgeNodeDescriptor(), messageContext.getSeqNum());
		EdgeNodeDescriptor edgeNodeDescriptor = messageContext.getTopic().getEdgeNodeDescriptor();
		SparkplugEdgeNode sparkplugEdgeNode =
				EdgeNodeManager.getInstance().getSparkplugEdgeNode(messageContext.getTopic().getEdgeNodeDescriptor());
		if (sparkplugEdgeNode == null || !sparkplugEdgeNode.isOnline()) {
			requestRebirth(messageContext.getMqttServerName(), messageContext.getHostAppMqttClientId(),
					messageContext.getTopic().getEdgeNodeDescriptor());
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT,
					"Invalid state of the Sparkplug Edge Node when receiving a NDATA - "
							+ messageContext.getTopic().getSparkplugDescriptor() + " is offline");
		}

		sparkplugEdgeNode.handleSeq(messageContext.getPayload().getSeq());

		eventHandler.onNodeDataArrived(edgeNodeDescriptor);
		for (Metric metric : messageContext.getPayload().getMetrics()) {
			if (!metric.hasName() && metric.hasAlias()) {
				metric.setName(
						HostApplicationAliasMap.getInstance().getMetricName(edgeNodeDescriptor, metric.getAlias()));
			}

			eventHandler.onDataMetric(edgeNodeDescriptor, metric);
		}
		eventHandler.onNodeDataArrived(edgeNodeDescriptor);
	}

	protected void handleDeviceData(MessageContext messageContext) throws Exception {
		logger.debug("Processing DDATA from Device {} with Seq# {}", messageContext.getTopic().getSparkplugDescriptor(),
				messageContext.getSeqNum());
		EdgeNodeDescriptor edgeNodeDescriptor = messageContext.getTopic().getEdgeNodeDescriptor();
		DeviceDescriptor deviceDescriptor = (DeviceDescriptor) messageContext.getTopic().getSparkplugDescriptor();
		SparkplugEdgeNode sparkplugEdgeNode = EdgeNodeManager.getInstance().getSparkplugEdgeNode(edgeNodeDescriptor);
		SparkplugDevice sparkplugDevice =
				EdgeNodeManager.getInstance().getSparkplugDevice(edgeNodeDescriptor, deviceDescriptor);
		if (sparkplugDevice == null || !sparkplugEdgeNode.isOnline()) {
			requestRebirth(messageContext.getMqttServerName(), messageContext.getHostAppMqttClientId(),
					messageContext.getTopic().getEdgeNodeDescriptor());
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT,
					"Invalid state of the Sparkplug Device when receiving a DDATA - "
							+ messageContext.getTopic().getSparkplugDescriptor() + " is offline");
		}

		sparkplugEdgeNode.handleSeq(messageContext.getPayload().getSeq());

		eventHandler.onDeviceDataArrived(deviceDescriptor);
		for (Metric metric : messageContext.getPayload().getMetrics()) {
			if (!metric.hasName() && metric.hasAlias()) {
				metric.setName(
						HostApplicationAliasMap.getInstance().getMetricName(edgeNodeDescriptor, metric.getAlias()));
			}

			eventHandler.onBirthMetric(deviceDescriptor, metric);
		}
		eventHandler.onDeviceDataComplete(deviceDescriptor);
	}

	public void requestRebirth(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId,
			EdgeNodeDescriptor edgeNodeDescriptor) {
		requestRebirth(mqttServerName, hostAppMqttClientId, edgeNodeDescriptor, null);
	}

	public void requestRebirth(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId,
			EdgeNodeDescriptor edgeNodeDescriptor, SparkplugEdgeNode sparkplugEdgeNode) {
		try {
			Timer rebirthDelayTimer = rebirthTimers.get(edgeNodeDescriptor);
			if (rebirthDelayTimer == null) {
				logger.info("Requesting Rebirth from {}", edgeNodeDescriptor);
				rebirthDelayTimer = new Timer();
				rebirthTimers.put(edgeNodeDescriptor, rebirthDelayTimer);
				rebirthDelayTimer.schedule(new RebirthDelayTask(edgeNodeDescriptor), 5000);

				// Request a rebirth
				SparkplugBPayload cmdPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date())
						.addMetric(
								new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, true).createMetric())
						.createPayload();

				Topic cmdTopic = new Topic("spBv1.0", edgeNodeDescriptor, MessageType.NCMD);
				if (sparkplugEdgeNode != null) {
					// Set the Edge Node offline
					sparkplugEdgeNode.setOnline(false, new Date(), null, null);

					if (mqttServerName != null && sparkplugEdgeNode.getMqttServerName() != null
							&& mqttServerName.equals(sparkplugEdgeNode.getMqttServerName())) {
						logger.debug("On Rebirth request - Current Engine MQTT Server is unchanged: {}",
								mqttServerName);
					} else {
						logger.info("On Rebirth request - MQTT Server has changed: new={}, old={}", mqttServerName,
								sparkplugEdgeNode.getMqttServerName());
					}
					if (hostAppMqttClientId != null && sparkplugEdgeNode.getHostAppMqttClientId() != null
							&& hostAppMqttClientId.equals(sparkplugEdgeNode.getHostAppMqttClientId())) {
						logger.debug("On Rebirth request - Current Engine MQTT Client ID is unchanged: {}",
								hostAppMqttClientId);
					} else {
						logger.info("On Rebirth request - MQTT Client ID has changed: new={}, old={}",
								hostAppMqttClientId, sparkplugEdgeNode.getHostAppMqttClientId());
					}

					// Update the current Engine MQTT Server name and Client ID
					sparkplugEdgeNode.setMqttServerName(mqttServerName);
					sparkplugEdgeNode.setHostAppMqttClientId(hostAppMqttClientId);

					publishCommand(mqttServerName, hostAppMqttClientId, cmdTopic, cmdPayload);
				} else {
					logger.debug("Current Engine MQTT Server Name for unknown Edge Node: {}", mqttServerName);
					logger.debug("Current Engine MQTT Client ID for unknown Edge Node: {}", hostAppMqttClientId);
					publishCommand(mqttServerName, hostAppMqttClientId, cmdTopic, cmdPayload);
				}
			} else {
				logger.debug("Not requesting Rebirth since we have in the last 5 seconds");
			}
		} catch (Exception e) {
			logger.error("Failed to create Rebirth request", e);
			return;
		}
	}

	/**
	 * A TimerTask subclass for timers on issued rebirth requests.
	 */
	private class RebirthDelayTask extends TimerTask {
		private EdgeNodeDescriptor edgeNodeDescriptor;

		public RebirthDelayTask(EdgeNodeDescriptor edgeNodeDescriptor) {
			this.edgeNodeDescriptor = edgeNodeDescriptor;
		}

		public void run() {
			if (rebirthTimers.get(edgeNodeDescriptor) != null) {
				rebirthTimers.get(edgeNodeDescriptor).cancel();
				rebirthTimers.remove(edgeNodeDescriptor);
			}
		}
	}

	private void publishCommand(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId, Topic topic,
			SparkplugBPayload payload) throws Exception {
		commandPublisher.publishCommand(mqttServerName, hostAppMqttClientId, topic, payload);
	}
}
