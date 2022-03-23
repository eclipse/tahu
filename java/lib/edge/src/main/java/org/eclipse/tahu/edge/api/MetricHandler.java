/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge.api;

public interface MetricHandler {

	/**
	 * Returns the {@link String} representing the LWT topic to register in the MQTT CONNECT packet
	 * 
	 * @return the {@link String} representing the LWT topic to register in the MQTT CONNECT packet
	 */
	public String getDeathTopic();

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
}
