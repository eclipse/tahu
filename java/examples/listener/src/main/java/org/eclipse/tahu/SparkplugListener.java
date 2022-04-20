/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.util.TopicUtil;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SparkplugListener implements MqttCallbackExtended {

	// Configuration
	private String serverUrl = "tcp://localhost:1883";
	private String clientId = "SparkplugBListenerEdgeNode";
	private String username = "admin";
	private String password = "changeme";
	private MqttClient client;
	
	public static void main(String[] args) {
		SparkplugListener listener = new SparkplugListener();
		listener.run();
	}
	
	public void run() {
		try {
			// Connect to the MQTT Server
			MqttConnectOptions options = new MqttConnectOptions();
			options.setAutomaticReconnect(true);
			options.setCleanSession(true);
			options.setConnectionTimeout(30);
			options.setKeepAliveInterval(30);
			options.setUserName(username);
			options.setPassword(password.toCharArray());
			client = new MqttClient(serverUrl, clientId);
			client.setTimeToWait(5000);						// short timeout on failure to connect
			client.connect(options);
			client.setCallback(this);
			
			// Just listen to all DDATA messages on spAv1.0 topics and wait for inbound messages
			client.subscribe("spBv1.0/#", 0);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		System.out.println("Connected!");
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("The MQTT Connection was lost! - will auto-reconnect");
    }

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		Topic sparkplugTopic = TopicUtil.parseTopic(topic);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		System.out.println("Message Arrived on Sparkplug topic " + sparkplugTopic.toString());
		
		SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
		SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());
		
		// Convert the message to JSON and print to system.out
		try {
			String payloadString = mapper.writeValueAsString(inboundPayload);
			System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inboundPayload));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("Published message: " + token);
	}
}
