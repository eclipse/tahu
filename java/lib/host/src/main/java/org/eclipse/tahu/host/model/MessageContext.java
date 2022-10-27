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
