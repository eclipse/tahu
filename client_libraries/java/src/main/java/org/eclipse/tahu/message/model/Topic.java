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
	 * The combination of the Group ID and the Edge Node ID
	 */
	private final String edgeNodeDescriptor;

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
	 * The message type.
	 */
	private final MessageType type;

	/**
	 * A Constructor.
	 * 
	 * @param namespace the namespace.
	 * @param groupId the group ID.
	 * @param edgeNodeId the edge node ID.
	 * @param deviceId the device ID.
	 * @param type the message type.
	 */
	public Topic(String namespace, String groupId, String edgeNodeId, String deviceId, MessageType type) {
		super();
		this.namespace = namespace;
		this.edgeNodeDescriptor = groupId + "/" + edgeNodeId;
		this.groupId = groupId;
		this.edgeNodeId = edgeNodeId;
		this.deviceId = deviceId;
		this.type = type;
	}

	/**
	 * A Constructor.
	 * 
	 * @param namespace the namespace.
	 * @param groupId the group ID.
	 * @param edgeNodeId the edge node ID.
	 * @param type the message type.
	 */
	public Topic(String namespace, String groupId, String edgeNodeId, MessageType type) {
		super();
		this.namespace = namespace;
		this.edgeNodeDescriptor = groupId + "/" + edgeNodeId;
		this.groupId = groupId;
		this.edgeNodeId = edgeNodeId;
		this.deviceId = null;
		this.type = type;
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
	 * Returns the Sparkplug EdgeNodeDescriptor
	 * 
	 * @return the EdgeNodeDescriptor of the form group_id/edge_node_id
	 */
	public String getEdgeNodeDescriptor() {
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
	 * Returns the message type.
	 * 
	 * @return the message type
	 */
	public MessageType getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(getNamespace()).append("/").append(getGroupId()).append("/")
				.append(getType()).append("/").append(getEdgeNodeId());
		if (getDeviceId() != null) {
			sb.append("/").append(getDeviceId());
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
