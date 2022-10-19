/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
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
