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

public class DeviceId extends EdgeNodeId {

	private final String deviceName;
	private final String deviceIdString;

	public DeviceId(String groupName, String edgeNodeName, String deviceName) {
		super(groupName, edgeNodeName);
		this.deviceName = deviceName;
		this.deviceIdString =
				new StringBuilder().append(super.getEdgeNodeIdString()).append("/").append(deviceName).toString();
	}

	public DeviceId(String deviceIdString) {
		super(deviceIdString.substring(0, deviceIdString.lastIndexOf("/")));
		this.deviceName = deviceIdString.substring(deviceIdString.lastIndexOf("/") + 1);
		this.deviceIdString = deviceIdString;
	}

	public DeviceId(EdgeNodeId edgeNodeId, String deviceName) {
		super(edgeNodeId.getGroupName(), edgeNodeId.getEdgeNodeIdString());
		this.deviceName = deviceName;
		this.deviceIdString =
				new StringBuilder().append(super.getEdgeNodeIdString()).append("/").append(deviceName).toString();
	}

	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * Returns a {@link String} representing the Device's Id of the form: "<groupName>/<edgeNodeName>/<deviceName>".
	 * 
	 * @return a {@link String} representing the Device's Id.
	 */
	@Override
	public String getIdString() {
		return deviceIdString;
	}

	/**
	 * Returns a {@link String} representing the Device's Id of the form: "<groupName>/<edgeNodeName>/<deviceName>".
	 * 
	 * @return a {@link String} representing the Device's Id.
	 */
	public String getDeviceIdString() {
		return deviceIdString;
	}

	@Override
	public int hashCode() {
		return this.getDeviceIdString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof DeviceId) {
			return this.getDeviceIdString().equals(((DeviceId) object).getDeviceIdString());
		}
		return this.getDeviceIdString().equals(object);
	}

	@Override
	public String toString() {
		return getDeviceIdString();
	}
}
