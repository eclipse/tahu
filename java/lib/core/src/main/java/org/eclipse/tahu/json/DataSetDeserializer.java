/********************************************************************************
 * Copyright (c) 2014-2024 Cirrus Link Solutions and others
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
package org.eclipse.tahu.json;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.model.DataSet;
import org.eclipse.tahu.message.model.DataSet.DataSetBuilder;
import org.eclipse.tahu.message.model.DataSetDataType;
import org.eclipse.tahu.message.model.Row;
import org.eclipse.tahu.message.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserializer to handle deserialization when ObjectMapper's 'enableDefaultTyping' is set
 */
public class DataSetDeserializer extends StdDeserializer<DataSet> {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(DataSetDeserializer.class.getName());

	private static final String FIELD_SIZE = "numberOfColumns";
	private static final String FIELD_TYPES = "types";
	private static final String FIELD_NAMES = "columnNames";
	private static final String FIELD_ROWS = "rows";

	protected DataSetDeserializer() {
		super(DataSet.class);
	}

	/**
	 * Constructor.
	 *
	 * @param clazz
	 */
	protected DataSetDeserializer(Class<DataSet> clazz) {
		super(clazz);
	}

	@Override
	public DataSet deserialize(JsonParser parser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		JsonNode node = parser.getCodec().readTree(parser);
		long size = 0;
		if (Integer.class.isAssignableFrom(node.get(FIELD_SIZE).numberValue().getClass())) {
			size = (Integer) node.get(FIELD_SIZE).numberValue();
		} else if (Long.class.isAssignableFrom(node.get(FIELD_SIZE).numberValue().getClass())) {
			size = (Long) node.get(FIELD_SIZE).numberValue();
		} else {
			logger.error("Failed to handle class type for {}: {}", FIELD_SIZE,
					node.get(FIELD_SIZE).numberValue().getClass());
		}
		DataSetBuilder builder = new DataSetBuilder(size);
		JsonNode namesNode = node.get(FIELD_NAMES);
		if (namesNode.isArray()) {
			logger.trace("namesNode: {}", namesNode);
			if (namesNode != null && namesNode.size() > 1 && namesNode.get(0).isTextual() && namesNode.get(1).isArray()
					&& namesNode.get(0).textValue().startsWith("java.util.")) {
				for (JsonNode columnName : namesNode.get(1)) {
					logger.trace("Adding column name: {}", columnName);
					builder.addColumnName(columnName.textValue());
				}
			} else {
				for (JsonNode columnName : namesNode) {
					logger.trace("Adding column name: {}", columnName.textValue());
					builder.addColumnName(columnName.textValue());
				}
			}
		}
		JsonNode typesNode = node.get(FIELD_TYPES);
		List<DataSetDataType> typesList = new ArrayList<DataSetDataType>();
		if (typesNode.isArray()) {
			if (typesNode != null && typesNode.size() > 1 && typesNode.get(0).isTextual() && typesNode.get(1).isArray()
					&& "java.util.ArrayList".equals(typesNode.get(0).textValue())) {
				for (JsonNode datatypeNode : typesNode.get(1)) {
					logger.trace("Adding datatype: {}", datatypeNode.textValue());
					typesList.add(DataSetDataType.valueOf(datatypeNode.textValue()));
				}
			} else {
				for (JsonNode typeNode : typesNode) {
					logger.trace("Adding datatype: {}", typeNode.textValue());
					typesList.add(DataSetDataType.valueOf(typeNode.textValue()));
				}
			}
			builder.addTypes(typesList);
		}
		JsonNode rowsNode = node.get(FIELD_ROWS);
		if (rowsNode.isArray()) {
			if (rowsNode != null && rowsNode.size() > 1 && rowsNode.get(0).isTextual() && rowsNode.get(1).isArray()
					&& "java.util.ArrayList".equals(rowsNode.get(0).textValue())) {
				for (JsonNode subRowsNode : rowsNode.get(1)) {
					logger.trace("SUB FIELD_ROWS: {}", subRowsNode);
					if (subRowsNode != null && subRowsNode.size() > 1 && subRowsNode.get(0).isTextual()
							&& subRowsNode.get(1).isArray()
							&& "java.util.ArrayList".equals(subRowsNode.get(0).textValue())) {
						List<Value<?>> values = new ArrayList<Value<?>>();
						JsonNode dataRowNode = subRowsNode.get(1);
						for (int i = 0; i < size; i++) {
							JsonNode value = dataRowNode.get(i);
							DataSetDataType type = typesList.get(i);
							Value<?> valueFromNode = getValueFromNode(value, type);
							logger.trace("Adding value to data row: {} with type={}", value.toString(), type);
							values.add(valueFromNode);
						}
						logger.trace("Adding data row: {}", values);
						builder.addRow(new Row(values));
					}
				}
			} else {
				for (JsonNode rowNode : rowsNode) {
					List<Value<?>> values = new ArrayList<Value<?>>();
					for (int i = 0; i < size; i++) {
						JsonNode value = rowNode.get(i);
						logger.trace("Adding value to row: {}", value.toString());
						DataSetDataType type = typesList.get(i);
						values.add(getValueFromNode(value, type));
					}
					logger.trace("Adding row: {}", values);
					builder.addRow(new Row(values));
				}
			}
		}
		try {
			return builder.createDataSet();
		} catch (SparkplugException e) {
			logger.error("Error deserializing DataSet ", e);
		}
		return null;
	}

	/*
	 * Creates and returns a Value instance
	 */
	private Value<?> getValueFromNode(JsonNode nodeValue, DataSetDataType type) {
		if (nodeValue.isArray() && nodeValue.size() == 2 && nodeValue.get(0).isTextual()
				&& nodeValue.get(0).textValue().startsWith("java.")) {
			logger.debug("Getting value from array: type={} and nodeValue={}", type, nodeValue);
			nodeValue = nodeValue.get(1);
		} else {
			logger.debug("Getting value: type={} and nodeValue={}", type, nodeValue);
		}

		if (type.toIntValue() == DataSetDataType.Boolean.toIntValue()) {
			return new Value<Boolean>(type, (boolean) nodeValue.asBoolean());
		} else if (type.toIntValue() == DataSetDataType.DateTime.toIntValue()) {
			return new Value<Date>(type, new Date(nodeValue.asLong()));
		} else if (type.toIntValue() == DataSetDataType.Double.toIntValue()) {
			return new Value<Double>(type, nodeValue.asDouble());
		} else if (type.toIntValue() == DataSetDataType.Float.toIntValue()) {
			return new Value<Float>(type, (float) nodeValue.asDouble());
		} else if (type.toIntValue() == DataSetDataType.Int8.toIntValue()) {
			return new Value<Byte>(type, (byte) nodeValue.asInt());
		} else if (type.toIntValue() == DataSetDataType.Int16.toIntValue()) {
			return new Value<Short>(type, (short) nodeValue.asInt());
		} else if (type.toIntValue() == DataSetDataType.Int32.toIntValue()) {
			return new Value<Integer>(type, nodeValue.asInt());
		} else if (type.toIntValue() == DataSetDataType.Int64.toIntValue()) {
			return new Value<Long>(type, (long) nodeValue.asLong());
		} else if (type.toIntValue() == DataSetDataType.UInt8.toIntValue()) {
			return new Value<Short>(type, (short) nodeValue.asInt());
		} else if (type.toIntValue() == DataSetDataType.UInt16.toIntValue()) {
			return new Value<Integer>(type, nodeValue.asInt());
		} else if (type.toIntValue() == DataSetDataType.UInt32.toIntValue()) {
			return new Value<Long>(type, (long) nodeValue.asLong());
		} else if (type.toIntValue() == DataSetDataType.UInt64.toIntValue()) {
			return new Value<BigInteger>(type, BigInteger.valueOf(nodeValue.asLong()));
		} else if (type.toIntValue() == DataSetDataType.String.toIntValue()
				|| type.toIntValue() == DataSetDataType.Text.toIntValue()) {
			return new Value<String>(type, nodeValue.asText());
		} else {
			return null;
		}
	}
}
