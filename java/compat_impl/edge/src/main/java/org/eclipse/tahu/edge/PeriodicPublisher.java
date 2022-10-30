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

package org.eclipse.tahu.edge;

import java.util.List;

import org.eclipse.tahu.edge.sim.DataSimulator;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodicPublisher implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(PeriodicPublisher.class.getName());

	private final long period;
	private final DataSimulator dataSimulator;
	private final EdgeClient edgeClient;
	private final EdgeNodeDescriptor edgeNodeDescriptor;
	private final List<DeviceDescriptor> deviceDescriptors;

	private volatile boolean stayRunning;

	public PeriodicPublisher(long period, DataSimulator dataSimulator, EdgeClient edgeClient,
			EdgeNodeDescriptor edgeNodeDescriptor, List<DeviceDescriptor> deviceDescriptors) {
		this.period = period;
		this.dataSimulator = dataSimulator;
		this.edgeClient = edgeClient;
		this.edgeNodeDescriptor = edgeNodeDescriptor;
		this.deviceDescriptors = deviceDescriptors;
		this.stayRunning = true;
	}

	@Override
	public void run() {
		try {
			while (stayRunning) {
				// Sleep a bit
				Thread.sleep(period);

				SparkplugBPayload nDataPayload = dataSimulator.getNodeDataPayload(edgeNodeDescriptor);
				edgeClient.publishNodeData(nDataPayload);

				for (DeviceDescriptor deviceDescriptor : deviceDescriptors) {
					SparkplugBPayload dDataPayload = dataSimulator.getDeviceDataPayload(deviceDescriptor);
					edgeClient.publishDeviceData(deviceDescriptor.getDeviceId(), dDataPayload);
				}
			}
		} catch (InterruptedException e) {
			logger.error("Failed to continue periodic publishing");
		}
	}

	public void shutdown() {
		stayRunning = false;
	}
}
