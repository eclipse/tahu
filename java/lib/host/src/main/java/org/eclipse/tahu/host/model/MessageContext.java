/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host.model;

import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;

/**
 * A container class to carry fields and objects associated with an MQTT message context.
 */
public class MessageContext {

	private final MqttServerName mqttServerName;
	private final MqttClientId hostAppMqttClientId;
	private final Topic topic;
	private final int payloadLength;
	private final SparkplugBPayload payload;
	private final long seqNum;

	public MessageContext(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId, Topic topic,
			int payloadLength, SparkplugBPayload payload, long seqNum) {
		this.mqttServerName = mqttServerName;
		this.hostAppMqttClientId = hostAppMqttClientId;
		this.topic = topic;
		this.payloadLength = payloadLength;
		this.payload = payload;
		this.seqNum = seqNum;
	}

	public MqttServerName getMqttServerName() {
		return mqttServerName;
	}

	public MqttClientId getHostAppMqttClientId() {
		return hostAppMqttClientId;
	}

	public Topic getTopic() {
		return topic;
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public SparkplugBPayload getPayload() {
		return payload;
	}

	public long getSeqNum() {
		return seqNum;
	}
}
