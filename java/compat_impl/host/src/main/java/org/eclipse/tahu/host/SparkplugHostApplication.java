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

package org.eclipse.tahu.host;

import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.host.api.HostApplicationEventHandler;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;
import org.eclipse.tahu.mqtt.MqttServerUrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkplugHostApplication implements HostApplicationEventHandler {

	private static Logger logger = LoggerFactory.getLogger(SparkplugHostApplication.class.getName());

	private static final String COMMAND_LISTENER_DIRECTORY = "/tmp/commands";
	private static final long COMMAND_LISTENER_POLL_RATE = 50L;

	private static final String HOST_ID = "IamHost";
	private static final String MQTT_CLIENT_ID = "Tahu_Host_Application";
	private static final String MQTT_SERVER_NAME = "My MQTT Server";
	private static final String MQTT_SERVER_URL = "tcp://localhost:1883";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "changeme";
	private static final int KEEP_ALIVE_TIMEOUT = 30;

	private CommandListener commandListener;
	private HostApplication hostApplication;

	public static void main(String[] arg) {
		try {
			System.out.println("Starting the Sparkplug Host Application");
			System.out.println("\tSparkplug Host Application ID: " + HOST_ID);
			System.out.println("\tMQTT Client ID: " + MQTT_CLIENT_ID);
			System.out.println("\tMQTT Server Name: " + MQTT_SERVER_NAME);
			System.out.println("\tMQTT Server URL: " + MQTT_SERVER_URL);
			System.out.println("\tUsername: " + USERNAME);
			System.out.println("\tPassword: ********");
			System.out.println("\tKeep Alive Timeout: " + KEEP_ALIVE_TIMEOUT);
			System.out.println("\tCommand Listener Directory: " + COMMAND_LISTENER_DIRECTORY);
			System.out.println("\tCommand Listener Poll Rate: " + COMMAND_LISTENER_POLL_RATE);

			// Start the Host Application
			SparkplugHostApplication sparkplugHostApplication = new SparkplugHostApplication();
			sparkplugHostApplication.start();

			// Sleep a while
			Thread.sleep(360000);

			// Shutdown
			sparkplugHostApplication.shutdown();

		} catch (Exception e) {
			logger.error("Failed to run the Edge Node", e);
		}
	}

	public SparkplugHostApplication() {
		try {

			hostApplication = new HostApplication(this, HOST_ID, new MqttClientId(MQTT_CLIENT_ID, false),
					new MqttServerName(MQTT_SERVER_NAME), new MqttServerUrl(MQTT_SERVER_URL), USERNAME, PASSWORD,
					KEEP_ALIVE_TIMEOUT, null);
		} catch (Exception e) {
			logger.error("Failed to create the HostApplication", e);
		}
	}

	public void start() throws TahuException {
		commandListener = new CommandListener(hostApplication, COMMAND_LISTENER_DIRECTORY, COMMAND_LISTENER_POLL_RATE);
		commandListener.start();
		hostApplication.start();
	}

	public void shutdown() {
		commandListener.shutdown();
		commandListener = null;
		hostApplication.shutdown();
	}

	@Override
	public void onNodeBirthArrived(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeBirthArrived from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onNodeBirthComplete(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeBirthComplete from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onNodeDataArrived(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDataArrived from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onNodeDataComplete(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDataComplete from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onNodeDeath(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDeath from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onNodeDeathComplete(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDeathComplete from {}...", edgeNodeDescriptor);
	}

	@Override
	public void onDeviceBirthArrived(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceBirthArrived from {}...", deviceDescriptor);
	}

	@Override
	public void onDeviceBirthComplete(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceBirthComplete from {}...", deviceDescriptor);
	}

	@Override
	public void onDeviceDataArrived(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDataArrived from {}...", deviceDescriptor);
	}

	@Override
	public void onDeviceDataComplete(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDataComplete from {}...", deviceDescriptor);
	}

	@Override
	public void onDeviceDeath(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDeath from {}...", deviceDescriptor);
	}

	@Override
	public void onDeviceDeathComplete(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDeathComplete from {}...", deviceDescriptor);
	}

	@Override
	public void onBirthMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// TODO Auto-generated method stub
		logger.info("onBirthMetric from {} with metric={}...", sparkplugDescriptor, metric);
	}

	@Override
	public void onDataMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// TODO Auto-generated method stub
		logger.info("onDataMetric from {} with metric={}...", sparkplugDescriptor, metric);
	}

	public void onStale(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// TODO Auto-generated method stub
		logger.info("onStale from {} for {}...", sparkplugDescriptor, metric.getName());
	}
}
