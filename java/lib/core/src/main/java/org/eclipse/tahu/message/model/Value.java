/********************************************************************************
 * Copyright (c) 2014-2022 Cirrus Link Solutions and others
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

/**
 * A class representing a Sparkplug metric {@link DataSet} value
 */
public class Value<V> {

	private DataSetDataType type;
	private V value;

	/**
	 * Default Constructor
	 */
	public Value() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param type the {@link DataSetDataType} of this {@link Value}
	 * @param value the value of this {@link DataSet} value
	 */
	public Value(DataSetDataType type, V value) {
		super();
		this.type = type;
		this.value = value;
	}

	/**
	 * The {@link DataSetDataType} of this {@link Value}
	 *
	 * @return the {@link DataSetDataType} of this {@link Value}
	 */
	public DataSetDataType getType() {
		return type;
	}

	/**
	 * Sets the {@link DataSetDataType} of this {@link Value}
	 *
	 * @param type the {@link DataSetDataType} to set for this {@link Value}
	 */
	public void setType(DataSetDataType type) {
		this.type = type;
	}

	/**
	 * The value of this {@link Value}
	 *
	 * @return the value of this {@link Value}
	 */
	public V getValue() {
		return value;
	}

	/**
	 * Sets the value of this {@link Value}
	 *
	 * @param type the value to set for this {@link Value}
	 */
	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Value other = (Value) obj;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Value [type=");
		builder.append(type);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
