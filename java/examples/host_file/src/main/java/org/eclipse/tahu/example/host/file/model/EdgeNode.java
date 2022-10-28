/********************************************************************************
 * Copyright (c) 2018-2022 Cirrus Link Solutions and others
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

package org.eclipse.tahu.example.host.file.model;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;

public class EdgeNode {

	private final String groupName;
	private final String edgeNodeName;
	private final EdgeNodeDescriptor edgeNodeDescriptor;

	private boolean online;
	private long lastSeqNumber;

	public EdgeNode(String groupName, String edgeNodeName) {
		this.groupName = groupName;
		this.edgeNodeName = edgeNodeName;
		this.edgeNodeDescriptor = new EdgeNodeDescriptor(groupName, edgeNodeName);
		this.online = false;
		this.lastSeqNumber = 255;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getEdgeNodeName() {
		return edgeNodeName;
	}

	public EdgeNodeDescriptor getEdgeNodeId() {
		return edgeNodeDescriptor;
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
		result = prime * result + ((edgeNodeDescriptor == null) ? 0 : edgeNodeDescriptor.hashCode());
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
		if (edgeNodeDescriptor == null) {
			if (other.edgeNodeDescriptor != null)
				return false;
		} else if (!edgeNodeDescriptor.equals(other.edgeNodeDescriptor))
			return false;
		return true;
	}
}
