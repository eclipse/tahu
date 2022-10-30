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

package org.eclipse.tahu.edge.sim;

import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugDescriptor;

public interface DataSimulator {

	/**
	 * Getting for fetching a NBIRTH {@link SparkplugBPayloadMap}
	 * 
	 * @param sparkplugDescriptor the {@link EdgeNodeDescriptor} to use when fetching the {@link SparkplugBPayloadMap}
	 * 
	 * @return a {@link SparkplugBPayloadMap} representing an NBIRTH payload
	 */
	public SparkplugBPayloadMap getNodeBirthPayload(EdgeNodeDescriptor edgeNodeDescriptor);

	/**
	 * Getting for fetching a NDATA {@link SparkplugBPayload}
	 *
	 * @param sparkplugDescriptor the {@link EdgeNodeDescriptor} to use when fetching the {@link SparkplugBPayloadMap}
	 *
	 * @return a {@link SparkplugBPayload} representing an NDATA payload
	 */
	public SparkplugBPayload getNodeDataPayload(EdgeNodeDescriptor edgeNodeDescriptor);

	/**
	 * Getting for fetching a DBIRTH {@link SparkplugBPayload}
	 * 
	 * @param deviceDescriptor the {@link DeviceDescriptor} to use when fetching the {@link SparkplugBPayload}
	 * 
	 * @return a {@link SparkplugBPayload} representing an DBIRTH payload
	 */
	public SparkplugBPayload getDeviceBirthPayload(DeviceDescriptor deviceDescriptor);

	/**
	 * Getting for fetching a DDATA {@link SparkplugBPayload}
	 * 
	 * @param deviceDescriptor the {@link DeviceDescriptor} to use when fetching the {@link SparkplugBPayload}
	 * 
	 * @return a {@link SparkplugBPayload} representing an DDATA payload
	 */
	public SparkplugBPayload getDeviceDataPayload(DeviceDescriptor deviceDescriptor);

	/**
	 * 
	 * @param sparkplugDescriptor
	 * @param metricName
	 * @return
	 */
	public boolean hasMetric(SparkplugDescriptor sparkplugDescriptor, String metricName);

	public Metric handleMetricWrite(SparkplugDescriptor sparkplugDescriptor, Metric metric);
}
