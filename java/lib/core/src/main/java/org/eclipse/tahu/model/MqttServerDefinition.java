/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.model;

import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;

public class MqttServerDefinition {

	private final MqttServerName mqttServerName;
	private final MqttClientId mqttClientId;
	private final MqttServerUrl mqttServerUrl;
	private final String username;
	private final String password;
	private final int keepAliveTimeout;
	private final Topic ndeathTopic;

	public MqttServerDefinition(MqttServerName mqttServerName, MqttClientId mqttClientId, MqttServerUrl mqttServerUrl,
			String username, String password, int keepAliveTimeout, Topic ndeathTopic) {
		this.mqttServerName = mqttServerName;
		this.mqttClientId = mqttClientId;
		this.mqttServerUrl = mqttServerUrl;
		this.username = username;
		this.password = password;
		this.keepAliveTimeout = keepAliveTimeout;
		this.ndeathTopic = ndeathTopic;
	}

	public MqttServerName getMqttServerName() {
		return mqttServerName;
	}

	public MqttClientId getMqttClientId() {
		return mqttClientId;
	}

	public MqttServerUrl getMqttServerUrl() {
		return mqttServerUrl;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getKeepAliveTimeout() {
		return keepAliveTimeout;
	}

	public Topic getNdeathTopic() {
		return ndeathTopic;
	}
}
