/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2017-2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.model;

/**
 * An Edge Node Identifier
 */
public class EdgeNodeId implements SparkplugId {

	private final String groupName;
	private final String edgeNodeName;
	private final String edgeNodeIdString;

	public EdgeNodeId(String groupName, String edgeNodeName) {
		this.groupName = groupName;
		this.edgeNodeName = edgeNodeName;
		this.edgeNodeIdString = new StringBuilder().append(groupName).append("/").append(edgeNodeName).toString();
	}

	/**
	 * Creates and EdgeNodeId from a {@link String} of the form group_name/edge_node_name
	 * 
	 * @param edgeNodeIdString the {@link String} representation of an EdgeNodeId
	 */
	public EdgeNodeId(String edgeNodeIdString) {
		String[] tokens = edgeNodeIdString.split("/");
		this.groupName = tokens[0];
		this.edgeNodeName = tokens[1];
		this.edgeNodeIdString = edgeNodeIdString;
	}

	public String getGroupName() {
		return groupName;
	}

	public String getEdgeNodeName() {
		return edgeNodeName;
	}

	/**
	 * Returns a {@link String} representing the Edge Node's Id of the form: "<groupName>/<edgeNodeName>".
	 * 
	 * @return a {@link String} representing the Edge Node's Id.
	 */
	@Override
	public String getIdString() {
		return edgeNodeIdString;
	}

	/**
	 * Returns a {@link String} representing the Edge Node's Id of the form: "<groupName>/<edgeNodeName>".
	 * 
	 * @return a {@link String} representing the Edge Node's Id.
	 */
	public String getEdgeNodeIdString() {
		return edgeNodeIdString;
	}

	@Override
	public int hashCode() {
		return this.getEdgeNodeIdString().hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof EdgeNodeId) {
			return this.getEdgeNodeIdString().equals(((EdgeNodeId) object).getEdgeNodeIdString());
		}
		return this.getEdgeNodeIdString().equals(object);
	}

	@Override
	public String toString() {
		return getEdgeNodeIdString();
	}
}
