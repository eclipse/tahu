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

public class StatePayload {

	@JsonProperty("online")
	private Boolean online;

	@JsonProperty("bdSeq")
	private Long bdSeq;

	@JsonProperty("timestamp")
	private Long timestamp;

	public StatePayload() {
		this.online = null;
		this.bdSeq = null;
		this.timestamp = null;
	}

	public StatePayload(Boolean online, Long bdSeq, Long timestamp) {
		super();
		this.online = online;
		this.bdSeq = bdSeq;
		this.timestamp = timestamp;
	}

	public Boolean isOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}

	public Long getBdSeq() {
		return bdSeq;
	}

	public void setBdSeq(Long bdSeq) {
		this.bdSeq = bdSeq;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StatePayload [online=");
		builder.append(online);
		builder.append(", bdSeq=");
		builder.append(bdSeq);
		builder.append(", timestamp=");
		builder.append(timestamp);
		builder.append("]");
		return builder.toString();
	}
}
