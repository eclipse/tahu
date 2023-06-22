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

package org.eclipse.tahu.host.api;

import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Message;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugDescriptor;

public interface HostApplicationEventHandler {

	public void onConnect();

	public void onDisconnect();

	public void onMessage(SparkplugDescriptor sparkplugDescriptor, Message message);

	public void onNodeBirthArrived(EdgeNodeDescriptor edgeNodeDescriptor, Message message);

	public void onNodeBirthComplete(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeDataArrived(EdgeNodeDescriptor edgeNodeDescriptor, Message message);

	public void onNodeDataComplete(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeDeath(EdgeNodeDescriptor edgeNodeDescriptor, Message message);

	public void onNodeDeathComplete(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onDeviceBirthArrived(DeviceDescriptor deviceDescriptor, Message message);

	public void onDeviceBirthComplete(DeviceDescriptor deviceDescriptor);

	public void onDeviceDataArrived(DeviceDescriptor deviceDescriptor, Message message);

	public void onDeviceDataComplete(DeviceDescriptor deviceDescriptor);

	public void onDeviceDeath(DeviceDescriptor deviceDescriptor, Message message);

	public void onDeviceDeathComplete(DeviceDescriptor deviceDescriptor);

	public void onBirthMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric);

	public void onDataMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric);

	public void onStale(SparkplugDescriptor sparkplugDescriptor, Metric metric);
}
