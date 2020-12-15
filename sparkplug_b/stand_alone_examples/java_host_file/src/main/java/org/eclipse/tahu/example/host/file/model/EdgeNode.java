/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2018-2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.example.host.file.model;

import org.eclipse.tahu.message.model.EdgeNodeId;

public class EdgeNode {

	private final String groupName;
	private final String edgeNodeName;
	private final EdgeNodeId edgeNodeId;

	private boolean online;
	private long lastSeqNumber;

	public EdgeNode(String groupName, String edgeNodeName) {
		this.groupName = groupName;
		this.edgeNodeName = edgeNodeName;
		this.edgeNodeId = new EdgeNodeId(groupName, edgeNodeName);
		this.online = false;
		this.lastSeqNumber = 255;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getEdgeNodeName() {
		return edgeNodeName;
	}

	public EdgeNodeId getEdgeNodeId() {
		return edgeNodeId;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public long getLastSeqNumber() {
		return lastSeqNumber;
	}

	public void setLastSeqNumber(long lastSeqNumber) {
		this.lastSeqNumber = lastSeqNumber;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((edgeNodeId == null) ? 0 : edgeNodeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EdgeNode other = (EdgeNode) obj;
		if (edgeNodeId == null) {
			if (other.edgeNodeId != null)
				return false;
		} else if (!edgeNodeId.equals(other.edgeNodeId))
			return false;
		return true;
	}
}
