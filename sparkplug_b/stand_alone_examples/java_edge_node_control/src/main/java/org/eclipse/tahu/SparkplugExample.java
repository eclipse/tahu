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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Topic;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.util.TopicUtil;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SparkplugExample implements MqttCallbackExtended {
	
	private static final String NAMESPACE = "spBv1.0";
	
	static {
		Logger.getRootLogger().setLevel(Level.OFF);
	}

	// Configuration
	private String serverUrl = "tcp://localhost:1883";
	private String groupId;
	private String edgeNode;
	private String clientId = UUID.randomUUID().toString();
	private String username = "admin";
	private String password = "changeme";
	private MqttClient client;
	
	public SparkplugExample(String groupId, String edgeNodeId) {
		this.groupId = groupId;
		this.edgeNode = edgeNodeId;
	}
	
	public static void main(String[] args) {
		SparkplugExample example = new SparkplugExample(args[0], args[1]);
		example.run();
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
			client.setTimeToWait(2000);	
			client.setCallback(this);
			client.connect(options);
			
			// Subscribe to control/command messages for both the edge of network node and the attached devices
			client.subscribe(NAMESPACE + "/" + groupId + "/+/" + edgeNode, 0);
			client.subscribe(NAMESPACE + "/" + groupId + "/+/" + edgeNode + "/*", 0);
			
			// Loop to receive input commands
			while (true) {
				System.out.print("\n> ");
				
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				String line = br.readLine();

				handleCommand(line);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void handleCommand(String command) 
			throws SparkplugException, MqttPersistenceException, MqttException, IOException {
		String [] tokens = command.split(" ");
		
		if (tokens.length > 0) {
			String cmd = tokens[0];
			if (cmd.equals("")) {
				return;
			}
			if (cmd.equals("?") || cmd.toLowerCase().equals("help")) {
				// Help with commands
				System.out.println("\nCOMMANDS");
				System.out.println(" - rebirth: Publishes a rebirth command to the Edge Node");
				System.out.println("     usage: rebirth");
				return;
			} else if (cmd.toLowerCase().equals("rebirth")) {
				// Issue a rebirth
				client.publish(NAMESPACE + "/" + groupId + "/NCMD/" + edgeNode, 
						new SparkplugBPayloadEncoder().getBytes(new SparkplugBPayloadBuilder()
								.addMetric(new MetricBuilder("Node Control/Rebirth", MetricDataType.Boolean, true)
										.createMetric())
								.createPayload()), 
						0, false);
				return;
			}
			
		}
		
		System.out.println("\nInvalid command: " + command);
	}
	
	@Override
	public void connectComplete(boolean reconnect, String serverURI) {
		System.out.println("Connected!");
	}

	public void connectionLost(Throwable cause) {
		System.out.println("The MQTT Connection was lost! - will auto-reconnect");
    }

	public void messageArrived(String topic, MqttMessage message) throws Exception {
		Topic sparkplugTopic = TopicUtil.parseTopic(topic);
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_NULL);
		
		SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
		SparkplugBPayload inboundPayload = decoder.buildFromByteArray(message.getPayload());

		if (sparkplugTopic.isType(MessageType.NBIRTH)) {
			try {
				System.out.println("\n\nRecieved Node Birth");
				System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(inboundPayload));
				System.out.print("\n\n> ");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void deliveryComplete(IMqttDeliveryToken token) {
		//System.out.println("Published message: " + token);
	}
}
