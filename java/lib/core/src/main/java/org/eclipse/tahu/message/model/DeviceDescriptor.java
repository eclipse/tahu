/********************************************************************************
 * Copyright (c) 2020-2022 Cirrus Link Solutions and others
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

public class DeviceDescriptor extends EdgeNodeDescriptor {

	private final String deviceId;
	private final String descriptorString;

	/**
	 * Constructor
	 *
	 * @param groupId the Sparkplug Group ID associated with this {@link DeviceDescriptor}
	 * @param edgeNodeId the Sparkplug Edge Node ID associated with this {@link DeviceDescriptor}
	 * @param deviceId the Sparkplug Device ID associated with this {@link DeviceDescriptor}
	 */
	public DeviceDescriptor(String groupId, String edgeNodeId, String deviceId) {
		super(groupId, edgeNodeId);
		this.deviceId = deviceId;
		this.descriptorString = groupId + "/" + edgeNodeId + "/" + deviceId;
	}

	/**
	 * Constructor
	 *
	 * @param descriptorString a {@link String} representing the Sparkplug Device Descriptor and MUST be of the form
	 *            group_id/edge_node_id/device_id
	 */
	public DeviceDescriptor(String descriptorString) {
		super(descriptorString.substring(0, descriptorString.lastIndexOf("/")));
		this.deviceId = descriptorString.substring(descriptorString.lastIndexOf("/") + 1);
		this.descriptorString = descriptorString;
	}

	/**
	 * Constructor
	 *
	 * @param edgeNodeDescriptor an {@link EdgeNodeDescriptor} that is the parent Edge Node of this Device
	 *
	 * @param deviceId the Sparkplug Device ID associated with this {@link DeviceDescriptor}
	 */
	public DeviceDescriptor(EdgeNodeDescriptor edgeNodeDescriptor, String deviceId) {
		super(edgeNodeDescriptor.getGroupId(), edgeNodeDescriptor.getEdgeNodeId());
		this.deviceId = deviceId;
		this.descriptorString = edgeNodeDescriptor.getDescriptorString() + "/" + deviceId;
	}

	/**
	 * Returns true because this is a {@link DeviceDescriptor}
	 *
	 * @return true because this is a {@link DeviceDescriptor}
	 */
	@Override
	public boolean isDeviceDescriptor() {
		return true;
	}

	/**
	 * Gets the Sparkplug Device ID associated with this {@link DeviceDescriptor}
	 *
	 * @return the Sparkplug Device ID associated with this {@link DeviceDescriptor}
	 */
	@Override
	public String getDeviceId() {
		return deviceId;
	}

	/**
	 * Returns a {@link String} representing the Device's Descriptor of the form:
	 * "<groupName>/<edgeNodeName>/<deviceId>".
	 *
	 * @return a {@link String} representing the Device's Descriptor.
	 */
	@Override
	public String getDescriptorString() {
		return descriptorString;
	}

	/**
	 * Returns the {@link EdgeNodeDescriptor} associated with this DeviceDescriptor
	 *
	 * @return a {@link EdgeNodeDescriptor} representing the Device's parent Edge Node Descriptor.
	 */
	public EdgeNodeDescriptor getEdgeNodeDescriptor() {
		return super.getEdgeNodeDescriptor();
	}

	/**
	 * Returns a {@link String} representing the Device's parent Edge Node Descriptor of the form:
	 * "<groupName>/<edgeNodeName>".
	 *
	 * @return a {@link String} representing the Device's parent Edge Node Descriptor.
	 */
	public String getEdgeNodeDescriptorString() {
		return super.getDescriptorString();
	}

	@Override
	public int hashCode() {
		return this.getDescriptorString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof DeviceDescriptor) {
			return this.getDescriptorString().equals(((DeviceDescriptor) object).getDescriptorString());
		}
		return this.getDescriptorString().equals(object);
	}

	@Override
	public String toString() {
		return getDescriptorString();
	}
}
