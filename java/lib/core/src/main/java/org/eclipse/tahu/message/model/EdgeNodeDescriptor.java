/********************************************************************************
 * Copyright (c) 2017-2022 Cirrus Link Solutions and others
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

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An Edge Node Identifier
 */
public class EdgeNodeDescriptor implements SparkplugDescriptor {

	private final String groupId;
	private final String edgeNodeId;
	private final String descriptorString;

	public EdgeNodeDescriptor(String groupId, String edgeNodeId) {
		this.groupId = groupId;
		this.edgeNodeId = edgeNodeId;
		this.descriptorString = groupId + "/" + edgeNodeId;
	}

	/**
	 * Creates and EdgeNodeDescriptor from a {@link String} of the form group_name/edge_node_name
	 * 
	 * @param descriptorString the {@link String} representation of an EdgeNodeDescriptor
	 */
	public EdgeNodeDescriptor(String descriptorString) {
		String[] tokens = descriptorString.split("/");
		this.groupId = tokens[0];
		this.edgeNodeId = tokens[1];
		this.descriptorString = descriptorString;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getEdgeNodeId() {
		return edgeNodeId;
	}

	/**
	 * Returns a {@link String} representing the Edge Node's Descriptor of the form: "<groupId>/<edgeNodeId>".
	 *
	 * @return a {@link String} representing the Edge Node's Descriptor.
	 */
	@Override
	public String getDescriptorString() {
		return descriptorString;
	}

	@Override
	public int hashCode() {
		return this.getDescriptorString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof EdgeNodeDescriptor) {
			return this.getDescriptorString().equals(((EdgeNodeDescriptor) object).getDescriptorString());
		}
		return this.getDescriptorString().equals(object);
	}

	@Override
	@JsonValue
	public String toString() {
		return getDescriptorString();
	}
}
