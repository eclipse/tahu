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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.json.DataSetDeserializer;
import org.eclipse.tahu.message.model.Row.RowBuilder;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A data set that represents a table of data.
 */
@JsonDeserialize(using = DataSetDeserializer.class)
public class DataSet {

	private static Logger logger = LogManager.getLogger(DataSet.class.getName());

	/**
	 * The number of columns
	 */
	@JsonProperty("numberOfColumns")
	private long numOfColumns;

	/**
	 * A list containing the names of each column
	 */
	@JsonProperty("columnNames")
	private List<String> columnNames;

	/**
	 * A list containing the data types of each column
	 */
	@JsonProperty("types")
	private List<DataSetDataType> types;

	/**
	 * A list containing the rows in the data set
	 */
	private List<Row> rows;

	public DataSet() {
	}

	public DataSet(long numOfColumns, List<String> columnNames, List<DataSetDataType> types, List<Row> rows) {
		this.numOfColumns = numOfColumns;
		this.columnNames = columnNames;
		this.types = types;
		this.rows = rows;
	}

	public long getNumOfColumns() {
		return numOfColumns;
	}

	public void setNumOfColumns(long numOfColumns) {
		this.numOfColumns = numOfColumns;
	}

	public List<String> getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(List<String> columnNames) {
		this.columnNames = columnNames;
	}

	public void addColumnName(String columnName) {
		this.columnNames.add(columnName);
	}

	public List<Row> getRows() {
		return rows;
	}

	@JsonGetter("rows")
	public List<List<Object>> getRowsAsLists() {
		List<List<Object>> list = new ArrayList<List<Object>>(getRows().size());
		for (Row row : getRows()) {
			list.add(Row.toValues(row));
		}
		return list;
	}

	public void addRow(Row row) {
		this.rows.add(row);
	}

	public void addRow(int index, Row row) {
		this.rows.add(index, row);
	}

	public Row removeRow(int index) {
		return rows.remove(index);
	}

	public boolean removeRow(Row row) {
		return rows.remove(row);
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	public List<DataSetDataType> getTypes() {
		return types;
	}

	public void setTypes(List<DataSetDataType> types) {
		this.types = types;
	}

	public void addType(DataSetDataType type) {
		this.types.add(type);
	}

	public void addType(int index, DataSetDataType type) {
		this.types.add(index, type);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataSet [numOfColumns=");
		builder.append(numOfColumns);
		builder.append(", columnNames=");
		builder.append(columnNames);
		builder.append(", types=");
		builder.append(types);
		builder.append(", rows=");
		builder.append(rows);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link DataSet} instance.
	 */
	public static class DataSetBuilder {

		private long numOfColumns;
		private List<String> columnNames;
		private List<DataSetDataType> types;
		private List<Row> rows;

		public DataSetBuilder(long numOfColumns) {
			this.numOfColumns = numOfColumns;
			this.columnNames = new ArrayList<String>();
			this.types = new ArrayList<DataSetDataType>();
			this.rows = new ArrayList<Row>();
		}

		public DataSetBuilder(DataSet dataSet) {
			this.numOfColumns = dataSet.getNumOfColumns();
			this.columnNames = new ArrayList<String>(dataSet.getColumnNames());
			this.types = new ArrayList<DataSetDataType>(dataSet.getTypes());
			this.rows = new ArrayList<Row>(dataSet.getRows().size());
			for (Row row : dataSet.getRows()) {
				rows.add(new RowBuilder(row).createRow());
			}
		}

		public DataSetBuilder addColumnNames(Collection<String> columnNames) {
			this.columnNames.addAll(columnNames);
			return this;
		}

		public DataSetBuilder addColumnName(String columnName) {
			this.columnNames.add(columnName);
			return this;
		}

		public DataSetBuilder addType(DataSetDataType type) {
			this.types.add(type);
			return this;
		}

		public DataSetBuilder addTypes(Collection<DataSetDataType> types) {
			this.types.addAll(types);
			return this;
		}

		public DataSetBuilder addRow(Row row) {
			this.rows.add(row);
			return this;
		}

		public DataSetBuilder addRows(Collection<Row> rows) {
			this.rows.addAll(rows);
			return this;
		}

		public DataSet createDataSet() throws SparkplugException {
			logger.trace("Number of columns: " + numOfColumns);
			for (String columnName : columnNames) {
				logger.trace("\tcolumnName: " + columnName);
			}
			for (DataSetDataType type : types) {
				logger.trace("\ttypes: " + type);
			}
			for (Row row : rows) {
				logger.trace("\t\trow: " + row);
			}

			validate();
			return new DataSet(numOfColumns, columnNames, types, rows);
		}

		public void validate() throws SparkplugException {
			if (columnNames.size() != numOfColumns) {
				throw new SparkplugException("Invalid number of columns in data set column names: " + columnNames.size()
						+ " vs expected " + numOfColumns);
			}
			if (types.size() != numOfColumns) {
				throw new SparkplugException("Invalid number of columns in data set types: " + types.size()
						+ " vs expected: " + numOfColumns);
			}
			for (int i = 0; i < types.size(); i++) {
				for (Row row : rows) {
					List<Value<?>> values = row.getValues();
					if (values.size() != numOfColumns) {
						throw new SparkplugException("Invalid number of columns in data set row: " + values.size()
								+ " vs expected: " + numOfColumns);
					}
					types.get(i).checkType(row.getValues().get(i).getValue());
				}
			}
		}
	}
}
