/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host.seq;

import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;

public class SequenceReorderContext {

	private final String topicString;
	private final String[] splitTopic;
	private final Topic topic;
	private final MqttMessage message;
	private final SparkplugBPayload payload;
	private final MessageType messageType;
	private final MqttServerName mqttServerName;
	private final MqttClientId hostAppMqttClientId;
	private final long arrivedTime;

	public SequenceReorderContext(String topicString, Topic topic, MqttMessage message, SparkplugBPayload payload,
			MessageType messageType, MqttServerName mqttServerName, MqttClientId hostAppMqttClientId,
			long arrivedTime) {
		this.topicString = topicString;
		this.splitTopic = topicString.split("/");
		this.topic = topic;
		this.message = message;
		this.payload = payload;
		this.messageType = messageType;
		this.mqttServerName = mqttServerName;
		this.hostAppMqttClientId = hostAppMqttClientId;
		this.arrivedTime = arrivedTime;
	}

	public String getTopicString() {
		return topicString;
	}

	public String[] getSplitTopic() {
		return splitTopic;
	}

	public Topic getTopic() {
		return topic;
	}

	public MqttMessage getMessage() {
		return message;
	}

	public SparkplugBPayload getPayload() {
		return payload;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public MqttServerName getMqttServerName() {
		return mqttServerName;
	}

	public MqttClientId getHostAppMqttClientId() {
		return hostAppMqttClientId;
	}

	public long getArrivedTime() {
		return arrivedTime;
	}
}
