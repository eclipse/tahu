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

package org.eclipse.tahu.host.model;

import org.eclipse.tahu.message.model.Message;
import org.eclipse.tahu.message.model.Message.MessageBuilder;
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
	private final Message message;
	private final int payloadLength;
	private final long seqNum;

	public MessageContext(MqttServerName mqttServerName, MqttClientId hostAppMqttClientId, Topic topic,
			SparkplugBPayload payload, int payloadLength, long seqNum) {
		this.mqttServerName = mqttServerName;
		this.hostAppMqttClientId = hostAppMqttClientId;
		this.message = new MessageBuilder(topic, payload).build();
		this.payloadLength = payloadLength;
		this.seqNum = seqNum;
	}

	public MqttServerName getMqttServerName() {
		return mqttServerName;
	}

	public MqttClientId getHostAppMqttClientId() {
		return hostAppMqttClientId;
	}

	public Message getMessage() {
		return message;
	}

	public Topic getTopic() {
		return message.getTopic();
	}

	public SparkplugBPayload getPayload() {
		return message.getPayload();
	}

	public int getPayloadLength() {
		return payloadLength;
	}

	public long getSeqNum() {
		return seqNum;
	}
}
