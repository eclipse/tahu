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

package org.eclipse.tahu.message.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class to represent Sparkplug Host Application STATE payloads
 */
public class StatePayload {

	@JsonProperty("online")
	private Boolean online;

	@JsonProperty("timestamp")
	private Long timestamp;

	/**
	 * Default Constructor
	 */
	public StatePayload() {
		this.online = null;
		this.timestamp = null;
	}

	/**
	 * Constructor
	 *
	 * @param online whether or not this {@link StatePayload} is represented as online or not
	 * @param timestamp the timestamp of this STATE payload
	 */
	public StatePayload(Boolean online, Long timestamp) {
		super();
		this.online = online;
		this.timestamp = timestamp;
	}

	/**
	 * Gets the online status of this {@link StatePayload}
	 *
	 * @return true if this {@link StatePayload} is online, otherwise false
	 */
	public Boolean isOnline() {
		return online;
	}

	/**
	 * Sets the online status of this {@link StatePayload}
	 *
	 * @param online true if this payload is representing an online Host Application, otherwise false
	 */
	public void setOnline(Boolean online) {
		this.online = online;
	}

	/**
	 * Gets the timestamp of this {@link StatePayload} as the number of milliseconds since epoch
	 *
	 * @return the timestamp of this {@link StatePayload} as the number of milliseconds since epoch
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp of this {@link StatePayload} as the number of milliseconds since epoch
	 *
	 * @param timestamp the timestamp of this {@link StatePayload} to set as the number of milliseconds since epoch
	 */
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatePayload [online=");
		builder.append(online);
		builder.append(", timestamp=");
		builder.append(timestamp);
		builder.append("]");
		return builder.toString();
	}
}
