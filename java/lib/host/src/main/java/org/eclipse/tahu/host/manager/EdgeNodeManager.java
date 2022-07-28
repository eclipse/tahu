/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host.manager;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.mqtt.MqttClientId;
import org.eclipse.tahu.mqtt.MqttServerName;

public class EdgeNodeManager {

	private static EdgeNodeManager instance;

	private Map<EdgeNodeDescriptor, SparkplugEdgeNode> edgeNodeMap;

	private Map<DeviceDescriptor, SparkplugDevice> deviceMap;

	private final Object lock = new Object();

	private EdgeNodeManager() {
		edgeNodeMap = new ConcurrentHashMap<>();
		deviceMap = new ConcurrentHashMap<>();
	}

	public static EdgeNodeManager getInstance() {
		if (instance == null) {
			instance = new EdgeNodeManager();
		}

		return instance;
	}

	public SparkplugEdgeNode getSparkplugEdgeNode(EdgeNodeDescriptor edgeNodeDescriptor) {
		synchronized (lock) {
			return edgeNodeMap.get(edgeNodeDescriptor);
		}
	}

	public SparkplugEdgeNode addSparkplugEdgeNode(EdgeNodeDescriptor edgeNodeDescriptor, MqttServerName mqttServerName,
			MqttClientId hostAppMqttClientId) {
		synchronized (lock) {
			SparkplugEdgeNode sparkplugEdgeNode =
					new SparkplugEdgeNode(edgeNodeDescriptor, mqttServerName, hostAppMqttClientId);
			edgeNodeMap.put(edgeNodeDescriptor, sparkplugEdgeNode);
			return sparkplugEdgeNode;
		}
	}

	public SparkplugDevice getSparkplugDevice(DeviceDescriptor deviceDescriptor) {
		synchronized (lock) {
			return deviceMap.get(deviceDescriptor);
		}
	}

	public SparkplugDevice addSparkplugDevice(SparkplugEdgeNode sparkplugEdgeNode, DeviceDescriptor deviceDescriptor,
			Date onlineTimestamp) throws TahuException {
		synchronized (lock) {
			// Make sure there is a SparkplugEdgeNode already
			if (edgeNodeMap.get(sparkplugEdgeNode.getEdgeNodeDescriptor()) == null) {
				throw new TahuException(TahuErrorCode.INITIALIZATION_ERROR,
						"The SparkplugEdgeNode must already exist before adding a device");
			}
			SparkplugDevice sparkplugDevice = new SparkplugDevice(sparkplugEdgeNode, deviceDescriptor, onlineTimestamp);
			deviceMap.put(deviceDescriptor, sparkplugDevice);
			return sparkplugDevice;
		}
	}
}
