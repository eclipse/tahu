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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeNodeManager {

	private static Logger logger = LoggerFactory.getLogger(EdgeNodeManager.class.getName());

	private static EdgeNodeManager instance;

	private Map<EdgeNodeDescriptor, SparkplugEdgeNode> edgeNodeMap;

	private final Object lock = new Object();

	private EdgeNodeManager() {
		edgeNodeMap = new ConcurrentHashMap<>();
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

	public SparkplugDevice getSparkplugDevice(EdgeNodeDescriptor edgeNodeDescriptor,
			DeviceDescriptor deviceDescriptor) {
		synchronized (lock) {
			SparkplugEdgeNode sparkplugEdgeNode = edgeNodeMap.get(edgeNodeDescriptor);
			if (sparkplugEdgeNode != null) {
				return sparkplugEdgeNode.getSparkplugDevice(deviceDescriptor);
			} else {
				return null;
			}
		}
	}

	public SparkplugDevice addSparkplugDevice(EdgeNodeDescriptor edgeNodeDescriptor, DeviceDescriptor deviceDescriptor,
			Date onlineTimestamp) throws TahuException {
		synchronized (lock) {
			// Make sure there is a SparkplugEdgeNode already
			SparkplugEdgeNode sparkplugEdgeNode = edgeNodeMap.get(edgeNodeDescriptor);
			if (sparkplugEdgeNode == null) {
				throw new TahuException(TahuErrorCode.INITIALIZATION_ERROR,
						"The SparkplugEdgeNode must already exist before adding a device");
			} else {
				SparkplugDevice sparkplugDevice =
						new SparkplugDevice(sparkplugEdgeNode, deviceDescriptor, onlineTimestamp);
				sparkplugEdgeNode.addDevice(deviceDescriptor, sparkplugDevice);
				return sparkplugDevice;
			}
		}
	}
}
