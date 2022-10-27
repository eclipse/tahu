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

import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SparkplugDevice extends MetricManager {

	private static Logger logger = LoggerFactory.getLogger(SparkplugDevice.class.getName());

	// Static variables
	private final SparkplugEdgeNode sparkplugEdgeNode;
	private final DeviceDescriptor deviceDescriptor;
	private final String groupId;
	private final String edgeNodeId;
	private final String deviceId;

	// Dynamic variables
	private boolean online;
	private Date onlineTimestamp;
	private Date offlineTimestamp;

	SparkplugDevice(SparkplugEdgeNode sparkplugEdgeNode, String groupId, String edgeNodeId, String deviceId,
			Date onlineTimestamp) {
		this(sparkplugEdgeNode, new DeviceDescriptor(groupId, edgeNodeId, deviceId), onlineTimestamp);
	}

	SparkplugDevice(SparkplugEdgeNode sparkplugEdgeNode, DeviceDescriptor deviceDescriptor, Date onlineTimestamp) {
		this.sparkplugEdgeNode = sparkplugEdgeNode;
		this.deviceDescriptor = deviceDescriptor;
		this.groupId = deviceDescriptor.getGroupId();
		this.edgeNodeId = deviceDescriptor.getEdgeNodeId();
		this.deviceId = deviceDescriptor.getDeviceId();

		this.online = true;
		this.onlineTimestamp = onlineTimestamp;
	}

	@Override
	public SparkplugDescriptor getSparkplugDescriptor() {
		return deviceDescriptor;
	}

	public SparkplugEdgeNode getSparkplugEdgeNode() {
		return sparkplugEdgeNode;
	}

	public DeviceDescriptor getDeviceDescrptor() {
		return deviceDescriptor;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getEdgeNodeId() {
		return edgeNodeId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online, Date timestamp) {
		this.online = online;
		if (online) {
			logger.info("Device {} set online at {}", deviceDescriptor, timestamp);
			this.onlineTimestamp = timestamp;
		} else {
			logger.info("Device {} set offline at {}", deviceDescriptor, timestamp);
			this.offlineTimestamp = timestamp;
		}
	}

	public Date getOnlineTimestamp() {
		return onlineTimestamp;
	}

	public Date getOfflineTimestamp() {
		return offlineTimestamp;
	}
}
