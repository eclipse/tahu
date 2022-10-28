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

package org.eclipse.tahu.edge.api;

import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.message.model.Topic;

public interface MetricHandler {

	/**
	 * Returns the {@link String} representing the LWT topic to register in the MQTT CONNECT packet
	 * 
	 * @return the {@link String} representing the LWT topic to register in the MQTT CONNECT packet
	 */
	public Topic getDeathTopic();

	/**
	 * The {@link byte[]} representing the LWT bytes to register in the MQTT CONNECT packet
	 * 
	 * @return a {@link byte[]} representing the LWT bytes to register in the MQTT CONNECT packet
	 * @throws Exception
	 */
	public byte[] getDeathPayloadBytes() throws Exception;

	/**
	 * Publishes the required birth message(s) for an Edge Node
	 */
	public void publishBirthSequence();

	/**
	 * Checks whether or not this MetricHandler has a Metric by a given name for a given {@link SparkplugDesciptor}
	 * 
	 * @param sparkplugDescriptor the {@link SparkplugDescriptor} to scope the metricName search to
	 * @param metricName the metricName to look for
	 * @return
	 */
	public boolean hasMetric(SparkplugDescriptor sparkplugDescriptor, String metricName);
}
