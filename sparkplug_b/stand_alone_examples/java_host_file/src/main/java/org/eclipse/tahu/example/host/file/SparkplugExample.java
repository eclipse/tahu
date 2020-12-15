/********************************************************************************
 * Copyright (c) 2014-2020 Cirrus Link Solutions and others
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

package org.eclipse.tahu.example.host.file;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.SparkplugParsingException;
import org.eclipse.tahu.example.host.file.model.EdgeNode;
import org.eclipse.tahu.example.host.file.model.FilePublishStatus;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.model.EdgeNodeId;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.util.TopicUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example Sparkplug B application.
 */
public class SparkplugExample implements MqttCallbackExtended {

	private static Logger logger = LoggerFactory.getLogger(SparkplugExample.class.getName());

	public static final String NAMESPACE = "spBv1.0";
	private static final String HOST_NAMESPACE = "STATE";

	// Configuration
	private static final boolean USING_REAL_TLS = false;
	private String serverUrl = "tcp://localhost:1883";
	private String primaryHostId = "IamHost";
	private String clientId = "HostFileExample";
	private String username = "admin";
	private String password = "changeme";
	private ExecutorService executor;
	private MqttClient client;

	private final Map<EdgeNodeId, EdgeNode> edgeNodeMap;
	private final Map<EdgeNodeId, Timer> rebirthTimers;
	private final Map<String, FileAssembler> fileAssemblers;

	public static void main(String[] args) {
		SparkplugExample example = new SparkplugExample();
		example.run();
	}

	public SparkplugExample() {
		BasicConfigurator.configure();
		edgeNodeMap = new ConcurrentHashMap<>();
		rebirthTimers = new ConcurrentHashMap<>();
		fileAssemblers = new ConcurrentHashMap<>();
	}

	public void run() {
		try {
			// Thread pool for outgoing published messages
			executor = Executors.newFixedThreadPool(1);

			// Build up Host Will payload
			byte[] willPayload = "OFFLINE".getBytes();

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
			if (primaryHostId != null && !primaryHostId.isEmpty()) {
				options.setWill(HOST_NAMESPACE + "/" + primaryHostId, willPayload, 1, true);
			}
			client = new MqttClient(serverUrl, clientId);
			client.setTimeToWait(2000);
			client.setCallback(this); // short timeout on failure to connect
			client.connect(options);

			// Subscribe to control/command messages for both the edge of network node and the attached devices
			client.subscribe(NAMESPACE + "/#", 0);
			if (primaryHostId != null && !primaryHostId.isEmpty()) {
				client.subscribe(HOST_NAMESPACE + "/" + primaryHostId, 0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void publishHostBirth() {
		try {
			if (primaryHostId != null && !primaryHostId.isEmpty()) {
				logger.info("Publishing Host Birth");
				executor.execute(
						new Publisher(client, HOST_NAMESPACE + "/" + primaryHostId, "ONLINE".getBytes(), 1, true));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		logger.info("Connected! - publishing birth");
		publishHostBirth();
	}

	public void connectionLost(Throwable cause) {
		cause.printStackTrace();
		logger.info("The MQTT Connection was lost! - will auto-reconnect");
	}

	public void messageArrived(String stringTopic, MqttMessage message) throws Exception {
		if (stringTopic != null && stringTopic.startsWith(NAMESPACE)) {
			// Get the topic tokens
			String[] sparkplugTokens = stringTopic.split("/");

			// Parse the Topic
			Topic topic;
			try {
				topic = TopicUtil.parseTopic(sparkplugTokens);
			} catch (SparkplugParsingException e) {
				logger.error("Error parsing topic", e);
				return;
			}

			if (topic.isType(MessageType.NCMD) || topic.isType(MessageType.DCMD)) {
				logger.trace("Ignoring CMD message");
				return;
			}

			// Get the payload
			SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
			SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());

			// Get the EdgeNodeId
			EdgeNodeId edgeNodeId = new EdgeNodeId(topic.getGroupId(), topic.getEdgeNodeId());

			// Special case for NBIRTH
			EdgeNode edgeNode = edgeNodeMap.get(edgeNodeId);
			if (topic.getType().equals(MessageType.NBIRTH)) {
				edgeNode = new EdgeNode(topic.getGroupId(), topic.getEdgeNodeId());
				edgeNodeMap.put(edgeNodeId, edgeNode);
			}

			// Failed to handle the message
			if (edgeNode == null) {
				logger.warn("Unexpected message on topic {} - requesting Rebirth", topic);
				requestRebirth(edgeNodeId);
				return;
			}

			// Check the sequence number
			if (handleSeqNumberCheck(edgeNode, inboundPayload.getSeq())) {
				logger.info("Validated sequence number on topic: {}", topic);

				// Iterate over the metrics looking only for file metrics
				for (Metric metric : inboundPayload.getMetrics()) {
					if (MetricDataType.File.equals(metric.getDataType())) {
						handleFileMetric(edgeNode, topic.getDeviceId(), metric);
					} else {
						logger.debug("Ignoring non-file metric: {}", metric.getName());
					}
				}
			} else {
				logger.error("Failed sequence number check for {}/{}", topic.getGroupId(), topic.getEdgeNodeId());
			}
		} else if (stringTopic != null && stringTopic.startsWith(HOST_NAMESPACE)) {
			if ("OFFLINE".equals(new String(message.getPayload()))) {
				logger.warn("The MQTT Server incorrectly reported the primary host is offline - correcting");
				publishHostBirth();
			}
		} else {
			logger.debug("Ignoring non-Sparkplug messages");
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		logger.info("Published message: " + token);
	}

	private boolean handleSeqNumberCheck(EdgeNode edgeNode, long incomingSeqNum) {
		// Get the last stored sequence number
		Long storedSeqNum = edgeNode.getLastSeqNumber();
		// Conditionally wrap to 0
		long expectedSeqNum = storedSeqNum + 1 == 256 ? 0 : storedSeqNum + 1;
		// Check if current sequence number is valid
		if (incomingSeqNum != expectedSeqNum) {
			// Sequence number is INVALID, set Edge Node offline
			edgeNode.setOnline(false);
			// Request a rebirth
			requestRebirth(edgeNode.getEdgeNodeId());
			return false;
		} else {
			edgeNode.setLastSeqNumber(incomingSeqNum);
			return true;
		}
	}

	private void requestRebirth(EdgeNodeId edgeNodeId) {
		try {
			Timer rebirthDelayTimer = rebirthTimers.get(edgeNodeId);
			if (rebirthDelayTimer == null) {
				logger.info("Requesting Rebirth from {}", edgeNodeId);
				rebirthDelayTimer = new Timer();
				rebirthTimers.put(edgeNodeId, rebirthDelayTimer);
				rebirthDelayTimer.schedule(new RebirthDelayTask(edgeNodeId), 5000);

				EdgeNode edgeNode = edgeNodeMap.get(edgeNodeId);
				if (edgeNode != null) {
					// Set the Edge Node offline
					edgeNode.setOnline(false);
				}

				// Request a device rebirth
				String rebirthTopic =
						new Topic(NAMESPACE, edgeNodeId.getGroupName(), edgeNodeId.getEdgeNodeName(), MessageType.NCMD)
								.toString();
				SparkplugBPayload rebirthPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date())
						.addMetric(
								new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, true).createMetric())
						.createPayload();

				executor.execute(new Publisher(client, rebirthTopic, rebirthPayload, 0, false));
			} else {
				logger.debug("Not requesting Rebirth since we have in the last 5 seconds");
			}
		} catch (Exception e) {
			logger.error("Failed to create Rebirth request", e);
			return;
		}
	}

	private class RebirthDelayTask extends TimerTask {
		private EdgeNodeId edgeNodeId;

		public RebirthDelayTask(EdgeNodeId edgeNodeId) {
			this.edgeNodeId = edgeNodeId;
		}

		public void run() {
			if (rebirthTimers.get(edgeNodeId) != null) {
				rebirthTimers.get(edgeNodeId).cancel();
				rebirthTimers.remove(edgeNodeId);
			}
		}
	}

	private void handleFileMetric(EdgeNode edgeNode, String deviceName, Metric metric) {

		String fileAssemblerName = null;
		if (deviceName == null || deviceName.trim().isEmpty()) {
			fileAssemblerName = new StringBuilder().append(edgeNode.getEdgeNodeId().getEdgeNodeIdString()).append("/")
					.append(metric.getName()).toString();
		} else {
			fileAssemblerName = new StringBuilder().append(edgeNode.getEdgeNodeId().getEdgeNodeIdString()).append("/")
					.append(deviceName).append("/").append(metric.getName()).toString();
		}
		FileAssembler fileAssembler = fileAssemblers.containsKey(fileAssemblerName)
				? fileAssemblers.get(fileAssemblerName)
				: new FileAssembler(executor, client, fileAssemblerName, edgeNode);
		handleFileMetric(fileAssembler, metric);
	}

	/*
	 * Handles supplied metrics for the file assembler 
	 */
	private void handleFileMetric(FileAssembler fileAssembler, Metric metric) {
		FilePublishStatus filePublishStatus = fileAssembler.processMetric(metric);
		if (filePublishStatus == FilePublishStatus.CONTINUE) {
			if (!fileAssemblers.containsKey(fileAssembler.getName())) {
				fileAssemblers.put(fileAssembler.getName(), fileAssembler);
			}
		} else {
			if (fileAssemblers.containsKey(fileAssembler.getName())) {
				fileAssemblers.remove(fileAssembler.getName());
			}
		}
	}
}
