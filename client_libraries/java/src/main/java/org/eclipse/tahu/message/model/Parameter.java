/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
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

import java.util.Objects;

import org.eclipse.tahu.SparkplugInvalidTypeException;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * A class to represent a parameter associated with a template.
 */
public class Parameter {

	/**
	 * The name of the parameter
	 */
	@JsonProperty("name")
	private String name;

	/**
	 * The data type of the parameter
	 */
	@JsonProperty("type")
	private ParameterDataType type;

	/**
	 * The value of the parameter
	 */
	@JsonProperty("value")
	private Object value;

	public Parameter() {
	}

	/**
	 * Constructs a Parameter instance.
	 * 
	 * @param name The name of the parameter.
	 * @param type The type of the parameter.
	 * @param value The value of the parameter.
	 * @throws SparkplugInvalidTypeException
	 */
	public Parameter(String name, ParameterDataType type, Object value) throws SparkplugInvalidTypeException {
		this.name = name;
		this.type = type;
		this.value = value;
		this.type.checkType(value);
	}

	@JsonGetter("name")
	public String getName() {
		return name;
	}

	@JsonSetter("name")
	public void setName(String name) {
		this.name = name;
	}

	public ParameterDataType getType() {
		return type;
	}

	public void setType(ParameterDataType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		Parameter param = (Parameter) object;
		return Objects.equals(name, param.getName()) && Objects.equals(type, param.getType())
				&& Objects.equals(value, param.getValue());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Parameter [name=");
		builder.append(name);
		builder.append(", type=");
		builder.append(type);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
