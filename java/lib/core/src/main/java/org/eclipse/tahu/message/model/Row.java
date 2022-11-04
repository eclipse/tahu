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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class for representing a row of a data set.
 */
public class Row {

	private List<Value<?>> values;

	/**
	 * Default Constructor
	 */
	public Row() {
		this.values = new ArrayList<>();
	}

	/**
	 * Constructor
	 *
	 * @param values
	 */
	public Row(List<Value<?>> values) {
		this.values = values;
	}

	/**
	 * Gets a {@link List} of {@link Value}s in the {@link Row}
	 *
	 * @return a {@link List} of {@link Value}s in the {@link Row}
	 */
	public List<Value<?>> getValues() {
		return values;
	}

	/**
	 * Sets a {@link List} of {@link Value}s for the {@link Row}
	 *
	 * @param values a {@link List} of {@link Value}s to set for the {@link Row}
	 */
	public void setValues(List<Value<?>> values) {
		this.values = values;
	}

	/**
	 * Adds a {@link Value} to the end of the {@link Row}
	 *
	 * @param value a {@link Value} to the end of the {@link Row}
	 */
	public void addValue(Value<?> value) {
		this.values.add(value);
	}

	@Override
	public String toString() {
		return "Row [values=" + values + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((values == null) ? 0 : values.hashCode());
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
		Row other = (Row) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	/**
	 * Converts a {@link Row} instance to a {@link List} of Objects representing the values.
	 * 
	 * @param row a {@link Row} instance.
	 * @return a {@link List} of Objects.
	 */
	public static List<Object> toValues(Row row) {
		List<Object> list = new ArrayList<Object>(row.getValues().size());
		for (Value<?> value : row.getValues()) {
			list.add(value.getValue());
		}
		return list;
	}

	/**
	 * A builder for creating a {@link Row} instance.
	 */
	public static class RowBuilder {

		private List<Value<?>> values;

		public RowBuilder() {
			this.values = new ArrayList<Value<?>>();
		}

		public RowBuilder(Row row) {
			this.values = new ArrayList<Value<?>>(row.getValues());
		}

		public RowBuilder addValue(Value<?> value) {
			this.values.add(value);
			return this;
		}

		public RowBuilder addValues(Collection<Value<?>> values) {
			this.values.addAll(values);
			return this;
		}

		public Row createRow() {
			return new Row(values);
		}
	}
}
