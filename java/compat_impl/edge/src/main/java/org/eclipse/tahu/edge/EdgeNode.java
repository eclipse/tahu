/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.edge.api.MetricHandler;
import org.eclipse.tahu.edge.sim.DataSimulator;
import org.eclipse.tahu.edge.sim.RandomDataSimulator;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap.SparkplugBPayloadMapBuilder;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.message.model.SparkplugMeta;
import org.eclipse.tahu.mqtt.ClientCallback;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;
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
	private static final String NBIRTH_TOPIC =
			SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + GROUP_ID + "/NBIRTH/" + EDGE_NODE_ID;
	private static final String NDEATH_TOPIC =
			SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + GROUP_ID + "/NDEATH/" + EDGE_NODE_ID;

	/*
	 * Next Birth BD sequence number - same as last deathBdSeq
	 */
	private long birthBdSeq = 0;

	/*
	 * Next Death BD sequence number
	 */
	private long deathBdSeq = 0;

	private DataSimulator dataSimulator = new RandomDataSimulator(10, new HashMap<SparkplugDescriptor, Integer>() {

		private static final long serialVersionUID = 1L;

		{
			put(new DeviceDescriptor("G1/E1/D1"), 10);
			put(new DeviceDescriptor("G1/E1/D2"), 10);
			put(new DeviceDescriptor("G1/E1/D3"), 10);
		}
	});

	/*
	 * Lock for manipulating the sequence number
	 */
	private Object bdSeqLock = new Object();

	public static void main(String[] arg) {
		EdgeNode edgeNode = new EdgeNode();
		edgeNode.run();
	}

	private EdgeClient edgeClient;

	public EdgeNode() {
		try {
			edgeClient = new EdgeClient(this, EDGE_NODE_DESCRIPTOR, DEVICE_IDS, PRIMARY_HOST_ID, REBIRTH_DEBOUNCE_DELAY,
					new MqttClientId(MQTT_CLIENT_ID, false), MQTT_SERVER_NAME, MQTT_SERVER_URL, USERNAME, PASSWORD,
					KEEP_ALIVE, this, null);
			Thread thread = new Thread(edgeClient);
			thread.start();
		} catch (TahuException e) {
			logger.error("Failed to create the Sparkplug Edge Client", e);
		}
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
		}
	}

	// MetricHandler API
	@Override
	public String getDeathTopic() {
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
			SparkplugBPayloadMap nBirthPayload = dataSimulator.getNBirthPayload(EDGE_NODE_DESCRIPTOR);
			nBirthPayload = addBirthSeqNum(nBirthPayload);
			edgeClient.publishSparkplugMessage(NBIRTH_TOPIC, nBirthPayload, 0, false);

			for (String deviceId : DEVICE_IDS) {
				SparkplugBPayload dBirthPayload =
						dataSimulator.getDBirth(new DeviceDescriptor(EDGE_NODE_DESCRIPTOR, deviceId));
				edgeClient.publishSparkplugMessage(SparkplugMeta.SPARKPLUG_B_TOPIC_PREFIX + "/" + GROUP_ID + "/DBIRTH/"
						+ EDGE_NODE_ID + "/" + deviceId, dBirthPayload, 0, false);
			}
		} catch (Exception e) {
			logger.error("Failed to publish the BIRTH sequence", e);
		}
	}

	// ClientCallback API
	@Override
	public void shutdown() {
		logger.info("ClientCallback shutdown");
	}

	// ClientCallback API
	@Override
	public void messageArrived(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			String topic, MqttMessage message) {
		logger.info("{}: ClientCallback messageArrived on topic={}", clientId, topic);

		if (topic.startsWith("STATE/")) {
			logger.info("Got STATE message: {} :: {}", topic, new String(message.getPayload()));
			edgeClient.handleStateMessage(topic.substring(6), new String(message.getPayload()));
			return;
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
		synchronized (bdSeqLock) {
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
		synchronized (bdSeqLock) {
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
