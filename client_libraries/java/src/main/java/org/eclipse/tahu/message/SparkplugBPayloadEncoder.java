/********************************************************************************
 * Copyright (c) 2014-2020 Cirrus Link Solutions and others
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

package org.eclipse.tahu.message;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.tahu.message.model.DataSet;
import org.eclipse.tahu.message.model.DataSetDataType;
import org.eclipse.tahu.message.model.File;
import org.eclipse.tahu.message.model.MetaData;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.PropertyDataType;
import org.eclipse.tahu.message.model.PropertySet;
import org.eclipse.tahu.message.model.PropertyValue;
import org.eclipse.tahu.message.model.Row;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Value;
import org.eclipse.tahu.protobuf.SparkplugBProto;

import com.google.protobuf.ByteString;

/**
 * A {@link PayloadDecode} implementation for encoding Sparkplug B payloads.
 */
public class SparkplugBPayloadEncoder implements PayloadEncoder<SparkplugBPayload> {

	private static Logger logger = LogManager.getLogger(SparkplugBPayloadEncoder.class.getName());

	public SparkplugBPayloadEncoder() {
		super();
	}

	public byte[] getBytes(SparkplugBPayload payload) throws IOException {

		SparkplugBProto.Payload.Builder protoMsg = SparkplugBProto.Payload.newBuilder();

		// Set the timestamp
		if (payload.getTimestamp() != null) {
			protoMsg.setTimestamp(payload.getTimestamp().getTime());
		}

		// Set the sequence number
		protoMsg.setSeq(payload.getSeq());

		// Set the UUID if defined
		if (payload.getUuid() != null) {
			protoMsg.setUuid(payload.getUuid());
		}

		// Set the metrics
		for (Metric metric : payload.getMetrics()) {
			try {
				protoMsg.addMetrics(convertMetric(metric));
			} catch (Exception e) {
				logger.error("Failed to add metric: " + metric.getName());
				throw new RuntimeException(e);
			}
		}

		// Set the body
		if (payload.getBody() != null) {
			protoMsg.setBody(ByteString.copyFrom(payload.getBody()));
		}

		return protoMsg.build().toByteArray();
	}

	private SparkplugBProto.Payload.Metric.Builder convertMetric(Metric metric) throws Exception {

		// build a metric
		SparkplugBProto.Payload.Metric.Builder builder = SparkplugBProto.Payload.Metric.newBuilder();

		// set the basic parameters
		builder.setDatatype(metric.getDataType().toIntValue());
		builder = setMetricValue(builder, metric);

		// Set the name, data type, and value
		if (metric.hasName()) {
			builder.setName(metric.getName());
		} else {
			// name is an empty String by default and must be cleared
			builder.clearName();
		}

		// Set the alias
		if (metric.hasAlias()) {
			builder.setAlias(metric.getAlias());
		}

		// Set the timestamp
		if (metric.getTimestamp() != null) {
			builder.setTimestamp(metric.getTimestamp().getTime());
		}

		// Set isHistorical
		if (metric.getIsHistorical() != null) {
			builder.setIsHistorical(metric.isHistorical());
		}

		// Set isTransient
		if (metric.getIsTransient() != null) {
			builder.setIsTransient(metric.isTransient());
		}

		// Set isNull
		if (metric.getIsNull() != null) {
			builder.setIsNull(metric.isNull());
		}

		// Set the metadata
		if (metric.getMetaData() != null) {
			builder = setMetaData(builder, metric);
		}

		// Set the property set
		if (metric.getProperties() != null) {
			builder.setProperties(convertPropertySet(metric.getProperties()));
		}

		return builder;
	}

	private SparkplugBProto.Payload.Template.Parameter.Builder convertParameter(Parameter parameter) throws Exception {

		// build a metric
		SparkplugBProto.Payload.Template.Parameter.Builder builder =
				SparkplugBProto.Payload.Template.Parameter.newBuilder();

		if (logger.isTraceEnabled()) {
			logger.trace("Adding parameter: " + parameter.getName());
			logger.trace("            type: " + parameter.getType());
		}

		// Set the name
		builder.setName(parameter.getName());

		// Set the type and value
		builder = setParameterValue(builder, parameter);

		return builder;
	}

	private SparkplugBProto.Payload.PropertySet.Builder convertPropertySet(PropertySet propertySet) throws Exception {
		SparkplugBProto.Payload.PropertySet.Builder setBuilder = SparkplugBProto.Payload.PropertySet.newBuilder();

		Map<String, PropertyValue> map = propertySet.getPropertyMap();
		for (String key : map.keySet()) {
			SparkplugBProto.Payload.PropertyValue.Builder builder = SparkplugBProto.Payload.PropertyValue.newBuilder();
			PropertyValue value = map.get(key);
			PropertyDataType type = value.getType();
			builder.setType(type.toIntValue());
			if (value.getValue() == null) {
				builder.setIsNull(true);
			} else {
				switch (type) {
					case Boolean:
						builder.setBooleanValue((Boolean) value.getValue());
						break;
					case DateTime:
						builder.setLongValue(((Date) value.getValue()).getTime());
						break;
					case Double:
						builder.setDoubleValue((Double) value.getValue());
						break;
					case Float:
						builder.setFloatValue((Float) value.getValue());
						break;
					case Int8:
						builder.setIntValue((Byte) value.getValue());
						break;
					case Int16:
					case UInt8:
						builder.setIntValue((Short) value.getValue());
						break;
					case Int32:
					case UInt16:
						builder.setIntValue((Integer) value.getValue());
						break;
					case Int64:
					case UInt32:
						builder.setLongValue((Long) value.getValue());
						break;
					case UInt64:
						builder.setLongValue(((BigInteger) value.getValue()).longValue());
						break;
					case String:
					case Text:
						builder.setStringValue((String) value.getValue());
						break;
					case PropertySet:
						builder.setPropertysetValue(convertPropertySet((PropertySet) value.getValue()));
						break;
					case PropertySetList:
						List<?> setList = (List<?>) value.getValue();
						SparkplugBProto.Payload.PropertySetList.Builder listBuilder =
								SparkplugBProto.Payload.PropertySetList.newBuilder();
						for (Object obj : setList) {
							listBuilder.addPropertyset(convertPropertySet((PropertySet) obj));
						}
						builder.setPropertysetsValue(listBuilder);
						break;
					case Unknown:
					default:
						logger.error("Unknown DataType: " + value.getType());
						throw new Exception("Failed to convert value " + value.getType());
				}
			}
			setBuilder.addKeys(key);
			setBuilder.addValues(builder);
		}
		return setBuilder;
	}

	private SparkplugBProto.Payload.Template.Parameter.Builder setParameterValue(
			SparkplugBProto.Payload.Template.Parameter.Builder builder, Parameter parameter) throws Exception {
		ParameterDataType type = parameter.getType();
		builder.setType(type.toIntValue());
		Object value = parameter.getValue();
		switch (type) {
			case Boolean:
				builder.setBooleanValue(toBoolean(value));
				break;
			case DateTime:
				builder.setLongValue(((Date) value).getTime());
				break;
			case Double:
				builder.setDoubleValue((Double) value);
				break;
			case Float:
				builder.setFloatValue((Float) value);
				break;
			case Int8:
				builder.setIntValue((Byte) value);
				break;
			case Int16:
			case UInt8:
				builder.setIntValue((Short) value);
				break;
			case Int32:
			case UInt16:
				builder.setIntValue((Integer) value);
				break;
			case Int64:
			case UInt32:
				builder.setLongValue((Long) value);
				break;
			case UInt64:
				builder.setLongValue(((BigInteger) value).longValue());
				break;
			case Text:
			case String:
				if (value == null) {
					builder.setStringValue("");
				} else {
					builder.setStringValue((String) value);
				}
				break;
			case Unknown:
			default:
				logger.error("Unknown Type: " + type);
				throw new Exception("Failed to encode");

		}
		return builder;
	}

	private SparkplugBProto.Payload.Metric.Builder setMetricValue(SparkplugBProto.Payload.Metric.Builder metricBuilder,
			Metric metric) throws Exception {

		// Set the data type
		metricBuilder.setDatatype(metric.getDataType().toIntValue());

		if (metric.getValue() == null) {
			metricBuilder.setIsNull(true);
		} else {
			switch (metric.getDataType()) {
				case Boolean:
					metricBuilder.setBooleanValue(toBoolean(metric.getValue()));
					break;
				case DateTime:
					metricBuilder.setLongValue(((Date) metric.getValue()).getTime());
					break;
				case File:
					metricBuilder.setBytesValue(ByteString.copyFrom(((File) metric.getValue()).getBytes()));
					SparkplugBProto.Payload.MetaData.Builder metaDataBuilder =
							SparkplugBProto.Payload.MetaData.newBuilder();
					metaDataBuilder.setFileName(((File) metric.getValue()).getFileName());
					metricBuilder.setMetadata(metaDataBuilder);
					break;
				case Float:
					metricBuilder.setFloatValue((Float) metric.getValue());
					break;
				case Double:
					metricBuilder.setDoubleValue((Double) metric.getValue());
					break;
				case Int8:
					metricBuilder.setIntValue(((Byte) metric.getValue()).intValue());
					break;
				case Int16:
				case UInt8:
					metricBuilder.setIntValue(((Short) metric.getValue()).intValue());
					break;
				case Int32:
				case UInt16:
					metricBuilder.setIntValue((int) metric.getValue());
					break;
				case UInt32:
				case Int64:
					metricBuilder.setLongValue((Long) metric.getValue());
					break;
				case UInt64:
					metricBuilder.setLongValue(((BigInteger) metric.getValue()).longValue());
					break;
				case String:
				case Text:
				case UUID:
					metricBuilder.setStringValue((String) metric.getValue());
					break;
				case Bytes:
					metricBuilder.setBytesValue(ByteString.copyFrom((byte[]) metric.getValue()));
					break;
				case DataSet:
					DataSet dataSet = (DataSet) metric.getValue();
					SparkplugBProto.Payload.DataSet.Builder dataSetBuilder =
							SparkplugBProto.Payload.DataSet.newBuilder();

					dataSetBuilder.setNumOfColumns(dataSet.getNumOfColumns());

					// Column names
					List<String> columnNames = dataSet.getColumnNames();
					if (columnNames != null && !columnNames.isEmpty()) {
						for (String name : columnNames) {
							// Add the column name
							dataSetBuilder.addColumns(name);
						}
					}

					// Column types
					List<DataSetDataType> columnTypes = dataSet.getTypes();
					if (columnTypes != null && !columnTypes.isEmpty()) {
						for (DataSetDataType type : columnTypes) {
							// Add the column type
							dataSetBuilder.addTypes(type.toIntValue());
						}
					}

					// Dataset rows
					List<Row> rows = dataSet.getRows();
					if (rows != null && !rows.isEmpty()) {
						for (Row row : rows) {
							SparkplugBProto.Payload.DataSet.Row.Builder protoRowBuilder =
									SparkplugBProto.Payload.DataSet.Row.newBuilder();
							List<Value<?>> values = row.getValues();
							if (values != null && !values.isEmpty()) {
								for (Value<?> value : values) {
									// Add the converted element
									protoRowBuilder.addElements(convertDataSetValue(value));
								}

								dataSetBuilder.addRows(protoRowBuilder);
							}
						}
					}

					// Finally add the dataset
					metricBuilder.setDatasetValue(dataSetBuilder);
					break;
				case Template:
					Template template = (Template) metric.getValue();
					SparkplugBProto.Payload.Template.Builder templateBuilder =
							SparkplugBProto.Payload.Template.newBuilder();

					// Set isDefinition
					templateBuilder.setIsDefinition(template.isDefinition());

					// Set Version
					if (template.getVersion() != null) {
						templateBuilder.setVersion(template.getVersion());
					}

					// Set Template Reference
					if (template.getTemplateRef() != null) {
						templateBuilder.setTemplateRef(template.getTemplateRef());
					}

					// Set the template metrics
					if (template.getMetrics() != null) {
						for (Metric templateMetric : template.getMetrics()) {
							templateBuilder.addMetrics(convertMetric(templateMetric));
						}
					}

					// Set the template parameters
					if (template.getParameters() != null) {
						for (Parameter parameter : template.getParameters()) {
							templateBuilder.addParameters(convertParameter(parameter));
						}
					}

					// Add the template to the metric
					metricBuilder.setTemplateValue(templateBuilder);
					break;
				case Unknown:
				default:
					logger.error("Unknown DataType: " + metric.getDataType());
					throw new Exception("Failed to encode");

			}
		}
		return metricBuilder;
	}

	private SparkplugBProto.Payload.Metric.Builder setMetaData(SparkplugBProto.Payload.Metric.Builder metricBuilder,
			Metric metric) throws Exception {

		// If the builder has been built already - use it
		SparkplugBProto.Payload.MetaData.Builder metaDataBuilder = metricBuilder.getMetadataBuilder() != null
				? metricBuilder.getMetadataBuilder()
				: SparkplugBProto.Payload.MetaData.newBuilder();

		MetaData metaData = metric.getMetaData();
		if (metaData.getContentType() != null) {
			metaDataBuilder.setContentType(metaData.getContentType());
		}
		if (metaData.getSize() != null) {
			metaDataBuilder.setSize(metaData.getSize());
		}
		if (metaData.getSeq() != null) {
			metaDataBuilder.setSeq(metaData.getSeq());
		}
		if (metaData.getFileName() != null) {
			metaDataBuilder.setFileName(metaData.getFileName());
		}
		if (metaData.getFileType() != null) {
			metaDataBuilder.setFileType(metaData.getFileType());
		}
		if (metaData.getMd5() != null) {
			metaDataBuilder.setMd5(metaData.getMd5());
		}
		if (metaData.getDescription() != null) {
			metaDataBuilder.setDescription(metaData.getDescription());
		}
		metricBuilder.setMetadata(metaDataBuilder);

		return metricBuilder;
	}

	private SparkplugBProto.Payload.DataSet.DataSetValue.Builder convertDataSetValue(Value<?> value) throws Exception {
		SparkplugBProto.Payload.DataSet.DataSetValue.Builder protoValueBuilder =
				SparkplugBProto.Payload.DataSet.DataSetValue.newBuilder();

		// Set the value
		DataSetDataType type = value.getType();

		switch (type) {
			case Int8:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setIntValue((Byte) value.getValue());
				break;
			case Int16:
			case UInt8:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setIntValue((Short) value.getValue());
				break;
			case Int32:
			case UInt16:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setIntValue((Integer) value.getValue());
				break;
			case Int64:
			case UInt32:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setLongValue((Long) value.getValue());
				break;
			case UInt64:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setLongValue(((BigInteger) value.getValue()).longValue());
				break;
			case Float:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setFloatValue((Float) value.getValue());
				break;
			case Double:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setDoubleValue((Double) value.getValue());
				break;
			case String:
			case Text:
				if (value != null && value.getValue() != null) {
					protoValueBuilder.setStringValue((String) value.getValue());
				} else {
					logger.debug("String value for dataset is null");
					protoValueBuilder.setStringValue("null");
				}
				break;
			case Boolean:
				if (value == null || value.getValue() == null) {
					return protoValueBuilder;
				}
				protoValueBuilder.setBooleanValue(toBoolean(value.getValue()));
				break;
			case DateTime:
				try {
					protoValueBuilder.setLongValue(((Date) value.getValue()).getTime());
				} catch (NullPointerException npe) {
					// FIXME - remove after is_null is supported for dataset values
					logger.debug("Date in dataset was null - leaving it -9223372036854775808L");
					protoValueBuilder.setLongValue(-9223372036854775808L);
				}
				break;
			default:
				logger.error("Unknown DataType: " + value.getType());
				throw new Exception("Failed to convert value " + value.getType());
		}

		return protoValueBuilder;
	}

	private Boolean toBoolean(Object value) {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return ((Integer) value).intValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof Long) {
			return ((Long) value).longValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof Float) {
			return ((Float) value).floatValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof Double) {
			return ((Double) value).doubleValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof Short) {
			return ((Short) value).shortValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof Byte) {
			return ((Byte) value).byteValue() == 0 ? Boolean.FALSE : Boolean.TRUE;
		} else if (value instanceof String) {
			return Boolean.parseBoolean(value.toString());
		}
		return (Boolean) value;
	}
}
