/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host;

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

	private static final String HOST_ID = "IamHost";
	private static final String MQTT_CLIENT_ID = "Example Host Application";
	private static final String MQTT_SERVER_NAME = "My MQTT Server";
	private static final String MQTT_SERVER_URL = "tcp://localhost:1883";
	private static final String USERNAME = "admin";
	private static final String PASSWORD = "changeme";
	private static final int KEEP_ALIVE_TIMETOUT = 30;
	private static final int INITIAL_BD_SEQ_NUMBER = 0;

	private HostApplication hostApplication;

	public static void main(String[] arg) {
		try {
			SparkplugHostApplication sparkplugHostApplication = new SparkplugHostApplication();
			sparkplugHostApplication.start();
			Thread.sleep(10000);
			sparkplugHostApplication.shutdown();

		} catch (Exception e) {
			logger.error("Failed to run the Edge Node", e);
		}
	}

	public SparkplugHostApplication() {
		try {
			hostApplication = new HostApplication(this, HOST_ID, new MqttClientId(MQTT_CLIENT_ID, false),
					new MqttServerName(MQTT_SERVER_NAME), new MqttServerUrl(MQTT_SERVER_URL), USERNAME, PASSWORD,
					KEEP_ALIVE_TIMETOUT, null, INITIAL_BD_SEQ_NUMBER);
		} catch (Exception e) {
			logger.error("Failed to create the HostApplication", e);
		}
	}

	public void start() {
		hostApplication.start();
	}

	public void shutdown() {
		hostApplication.shutdown();
	}

	@Override
	public void onNodeBirthArrived(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeBirthArrived...");
	}

	@Override
	public void onNodeBirthComplete(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeBirthComplete...");
	}

	@Override
	public void onNodeDataArrived(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDataArrived...");
	}

	@Override
	public void onNodeDataComplete(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDataComplete...");
	}

	@Override
	public void onNodeDeath(EdgeNodeDescriptor edgeNodeDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onNodeDeath...");
	}

	@Override
	public void onDeviceBirthArrived(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceBirthArrived...");
	}

	@Override
	public void onDeviceBirthComplete(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceBirthComplete...");
	}

	@Override
	public void onDeviceDataArrived(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDataArrived...");
	}

	@Override
	public void onDeviceDataComplete(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDataComplete...");
	}

	@Override
	public void onDeviceDeath(DeviceDescriptor deviceDescriptor) {
		// TODO Auto-generated method stub
		logger.info("onDeviceDeath...");
	}

	@Override
	public void onBirthMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// TODO Auto-generated method stub
		logger.info("onBirthMetric...");
	}

	@Override
	public void onDataMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// TODO Auto-generated method stub
		logger.info("onDataMetric...");
	}
}
