/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host.api;

import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugDescriptor;

public interface HostApplicationEventHandler {

	public void onNodeBirthArrived(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeBirthComplete(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeDataArrived(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeDataComplete(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onNodeDeath(EdgeNodeDescriptor edgeNodeDescriptor);

	public void onDeviceBirthArrived(DeviceDescriptor deviceDescriptor);

	public void onDeviceBirthComplete(DeviceDescriptor deviceDescriptor);

	public void onDeviceDataArrived(DeviceDescriptor deviceDescriptor);

	public void onDeviceDataComplete(DeviceDescriptor deviceDescriptor);

	public void onDeviceDeath(DeviceDescriptor deviceDescriptor);

	public void onBirthMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric);

	public void onDataMetric(SparkplugDescriptor sparkplugDescriptor, Metric metric);
}
