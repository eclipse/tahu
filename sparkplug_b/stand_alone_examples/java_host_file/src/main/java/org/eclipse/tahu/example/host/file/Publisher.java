/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.example.host.file;

import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;

public class Publisher implements Runnable {

	private final MqttClient client;
	private final String topic;
	private final byte[] bytePayload;
	private final SparkplugBPayload sparkplugPayload;
	private final int qos;
	private final boolean retained;

	public Publisher(MqttClient client, String topic, byte[] bytePayload, int qos, boolean retained) {
		this.client = client;
		this.topic = topic;
		this.bytePayload = bytePayload;
		this.sparkplugPayload = null;
		this.qos = qos;
		this.retained = retained;
	}

	public Publisher(MqttClient client, String topic, SparkplugBPayload sparkplugPayload, int qos, boolean retained) {
		this.client = client;
		this.topic = topic;
		this.bytePayload = null;
		this.sparkplugPayload = sparkplugPayload;
		this.qos = qos;
		this.retained = retained;
	}

	public void run() {
		try {
			if (bytePayload != null) {
				client.publish(topic, bytePayload, qos, retained);
			} else if (sparkplugPayload != null) {
				sparkplugPayload.setTimestamp(new Date());
				SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
				client.publish(topic, encoder.getBytes(sparkplugPayload), qos, retained);
			} else {
				client.publish(topic, null, 0, false);
			}
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
