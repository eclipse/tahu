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

package org.eclipse.tahu.message.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A Sparkplug MQTT Topic
 */
@JsonInclude(Include.NON_NULL)
public class Topic {

	/**
	 * The Sparkplug namespace version.
	 */
	private final String namespace;

	/**
	 * The {@link SparkplugDesciptor} for this Edge Node or Device
	 */
	@JsonIgnore
	private final SparkplugDescriptor sparkplugDescriptor;

	/**
	 * The {@link EdgeNodeDescriptor} for this Edge Node or Device
	 */
	private final EdgeNodeDescriptor edgeNodeDescriptor;

	/**
	 * The ID of the logical grouping of Edge of Network (EoN) Nodes and devices.
	 */
	private final String groupId;

	/**
	 * The ID of the Edge of Network (EoN) Node.
	 */
	private final String edgeNodeId;

	/**
	 * The ID of the device.
	 */
	private final String deviceId;

	/**
	 * The ID if this is a Sparkplug Host Application topic
	 */
	private final String hostApplicationId;

	/**
	 * The message type.
	 */
	private final MessageType type;

	/**
	 * A Constructor for Device Topics
	 * 
	 * @param namespace the namespace
	 * @param groupId the Group ID
	 * @param edgeNodeId the Edge Node ID
	 * @param deviceId the Device ID
	 * @param hostApplicationId the Host Application ID
	 * @param type the message type
	 */
	public Topic(String namespace, String groupId, String edgeNodeId, String deviceId, MessageType type) {
		super();
		this.namespace = namespace;
		this.sparkplugDescriptor = deviceId == null
				? new EdgeNodeDescriptor(groupId, edgeNodeId)
				: new DeviceDescriptor(groupId, edgeNodeId, deviceId);
		this.edgeNodeDescriptor = new EdgeNodeDescriptor(groupId, edgeNodeId);
		this.groupId = groupId;
		this.edgeNodeId = edgeNodeId;
		this.deviceId = deviceId;
		this.hostApplicationId = null;
		this.type = type;
	}

	/**
	 * A Constructor for Edge Node Topics
	 * 
	 * @param namespace the namespace
	 * @param groupId the group ID
	 * @param edgeNodeId the edge node ID
	 * @param type the message type
	 */
	public Topic(String namespace, String groupId, String edgeNodeId, MessageType type) {
		super();
		this.namespace = namespace;
		this.sparkplugDescriptor = new EdgeNodeDescriptor(groupId, edgeNodeId);
		this.edgeNodeDescriptor = new EdgeNodeDescriptor(groupId, edgeNodeId);
		this.groupId = groupId;
		this.edgeNodeId = edgeNodeId;
		this.deviceId = null;
		this.hostApplicationId = null;
		this.type = type;
	}

	/**
	 * A Constructor for Device Topics
	 * 
	 * @param namespace the namespace
	 * @param deviceDescriptor the {@link EdgeNodeDescriptor}
	 * @param type the message type
	 */
	public Topic(String namespace, DeviceDescriptor deviceDescriptor, MessageType type) {
		this(namespace, deviceDescriptor.getGroupId(), deviceDescriptor.getEdgeNodeId(), deviceDescriptor.getDeviceId(),
				type);
	}

	/**
	 * A Constructor for Edge Node Topics
	 * 
	 * @param namespace the namespace
	 * @param edgeNodeDescriptor the {@link EdgeNodeDescriptor}
	 * @param type the message type
	 */
	public Topic(String namespace, EdgeNodeDescriptor edgeNodeDescriptor, MessageType type) {
		this(namespace, edgeNodeDescriptor.getGroupId(), edgeNodeDescriptor.getEdgeNodeId(), type);
	}

	/**
	 * A Constructor for Host Application Topics
	 *
	 * @param namespace the namespace
	 * @param hostApplicationId the Host Application ID
	 */
	public Topic(String namespace, String hostApplicationId, MessageType type) {
		super();
		this.namespace = namespace;
		this.hostApplicationId = hostApplicationId;
		this.type = type;
		this.sparkplugDescriptor = null;
		this.edgeNodeDescriptor = null;
		this.groupId = null;
		this.edgeNodeId = null;
		this.deviceId = null;
	}

	/**
	 * Returns the Sparkplug namespace version.
	 * 
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * Returns the {@link SparkplugDescriptor}
	 * 
	 * @return the SparkplugDescriptor
	 */
	public SparkplugDescriptor getSparkplugDescriptor() {
		return sparkplugDescriptor;
	}

	/**
	 * Returns the {@link EdgeNodeDescriptor}
	 * 
	 * @return the EdgeNodeDescriptor
	 */
	public EdgeNodeDescriptor getEdgeNodeDescriptor() {
		return edgeNodeDescriptor;
	}

	/**
	 * Returns the ID of the logical grouping of Edge of Network (EoN) Nodes and devices.
	 * 
	 * @return the group ID
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Returns the ID of the Edge of Network (EoN) Node.
	 * 
	 * @return the edge node ID
	 */
	public String getEdgeNodeId() {
		return edgeNodeId;
	}

	/**
	 * Returns the ID of the device.
	 * 
	 * @return the device ID
	 */
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Returns the Host Application ID if this is a Host topic
	 *
	 * @return the Host Application ID
	 */
	public String getHostApplicationId() {
		return hostApplicationId;
	}

	/**
	 * Returns the message type.
	 * 
	 * @return the message type
	 */
	public MessageType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (hostApplicationId == null) {
			sb.append(getNamespace()).append("/").append(getGroupId()).append("/").append(getType()).append("/")
					.append(getEdgeNodeId());
			if (getDeviceId() != null) {
				sb.append("/").append(getDeviceId());
			}
		} else {
			sb.append(getNamespace()).append("/").append(getType()).append("/").append(hostApplicationId);
		}
		return sb.toString();
	}

	/**
	 * Returns true if this topic's type matches the passes in type, false otherwise.
	 * 
	 * @param type the type to check
	 * @return true if this topic's type matches the passes in type, false otherwise
	 */
	public boolean isType(MessageType type) {
		return this.type != null && this.type.equals(type);
	}
}
