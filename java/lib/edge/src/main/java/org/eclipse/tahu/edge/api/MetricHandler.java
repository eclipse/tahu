/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
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
