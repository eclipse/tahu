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

import java.util.Date;

import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.DataSet.DataSetBuilder;
import org.eclipse.tahu.message.model.MetaData.MetaDataBuilder;
import org.eclipse.tahu.message.model.PropertySet.PropertySetBuilder;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

/**
 * A metric of a Sparkplug Payload.
 */
@JsonIgnoreProperties(value = { "isNull" })
@JsonInclude(Include.NON_NULL)
public class Metric {

	@JsonProperty("name")
	private String name;

	@JsonProperty("alias")
	private Long alias;

	@JsonProperty("timestamp")
	private Date timestamp;

	@JsonProperty("dataType")
	private MetricDataType dataType;

	@JsonProperty("isHistorical")
	private Boolean isHistorical;

	@JsonProperty("isTransient")
	private Boolean isTransient;

	@JsonProperty("metaData")
	private MetaData metaData;

	@JsonProperty("properties")
	@JsonInclude(Include.NON_EMPTY)
	private PropertySet properties;

	@JsonProperty("value")
	private Object value;

	private Boolean isNull = null;

	public Metric() {
	};

	/**
	 * @param name
	 * @param alias
	 * @param timestamp
	 * @param dataType
	 * @param isHistorical
	 * @param isTransient
	 * @param isNull
	 * @param metaData
	 * @param properties
	 * @param value
	 * @throws SparkplugInvalidTypeException
	 */
	public Metric(String name, Long alias, Date timestamp, MetricDataType dataType, Boolean isHistorical,
			Boolean isTransient, MetaData metaData, PropertySet properties, Object value)
			throws SparkplugInvalidTypeException {
		super();
		this.name = name;
		this.alias = alias;
		this.timestamp = timestamp;
		this.dataType = dataType;
		this.isHistorical = isHistorical;
		this.isTransient = isTransient;
		isNull = (value == null) ? true : false;
		this.metaData = metaData;
		this.properties = properties;
		this.value = value;
		this.dataType.checkType(value);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasName() {
		return !(name == null);
	}

	public boolean hasAlias() {
		return !(alias == null);
	}

	public Long getAlias() {
		return alias;
	}

	public void setAlias(long alias) {
		this.alias = alias;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public MetricDataType getDataType() {
		return dataType;
	}

	public void setDataType(MetricDataType dataType) {
		this.dataType = dataType;
	}

	@JsonGetter("metaData")
	public MetaData getMetaData() {
		return metaData;
	}

	@JsonSetter("metaData")
	public void setMetaData(MetaData metaData) {
		this.metaData = metaData;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
		isNull = (value == null);
	}

	public PropertySet getProperties() {
		return this.properties;
	}

	public void setProperties(PropertySet properties) {
		this.properties = properties;
	}

	@JsonIgnore
	public Boolean isHistorical() {
		return isHistorical == null ? false : isHistorical;
	}

	@JsonGetter("isHistorical")
	public Boolean getIsHistorical() {
		return isHistorical;
	}

	@JsonSetter("isHistorical")
	public void setHistorical(Boolean isHistorical) {
		this.isHistorical = isHistorical;
	}

	@JsonIgnore
	public Boolean isTransient() {
		return isTransient == null ? false : isTransient;
	}

	@JsonGetter("isTransient")
	public Boolean getIsTransient() {
		return isTransient;
	}

	@JsonSetter("isTransient")
	public void setTransient(Boolean isTransient) {
		this.isTransient = isTransient;
	}

	@JsonIgnore
	public Boolean isNull() {
		return isNull == null ? false : isNull;
	}

	@JsonIgnore
	public Boolean getIsNull() {
		return isNull;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Metric [name=");
		builder.append(name);
		builder.append(", alias=");
		builder.append(alias);
		builder.append(", timestamp=");
		builder.append(timestamp);
		builder.append(", dataType=");
		builder.append(dataType);
		builder.append(", isHistorical=");
		builder.append(isHistorical);
		builder.append(", isTransient=");
		builder.append(isTransient);
		builder.append(", metaData=");
		builder.append(metaData);
		builder.append(", properties=");
		builder.append(properties);
		builder.append(", value=");
		builder.append(value);
		builder.append(", isNull=");
		builder.append(isNull);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link Metric} instance.
	 */
	public static class MetricBuilder {

		private String name;
		private Long alias;
		private Date timestamp;
		private MetricDataType dataType;
		private Boolean isHistorical;
		private Boolean isTransient;
		private MetaData metaData = null;
		private PropertySet properties = null;
		private Object value;

		public MetricBuilder(String name, MetricDataType dataType, Object value) {
			this.name = name;
			this.timestamp = new Date();
			this.dataType = dataType;
			this.value = value;
		}

		public MetricBuilder(Long alias, MetricDataType dataType, Object value) {
			this.alias = alias;
			this.timestamp = new Date();
			this.dataType = dataType;
			this.value = value;
		}

		public MetricBuilder(Metric metric) throws SparkplugException {
			this.name = metric.getName();
			this.alias = metric.getAlias();
			this.timestamp = metric.getTimestamp();
			this.dataType = metric.getDataType();
			this.isHistorical = metric.isHistorical();
			this.isTransient = metric.isTransient();
			this.metaData =
					metric.getMetaData() != null ? new MetaDataBuilder(metric.getMetaData()).createMetaData() : null;
			this.properties = metric.getMetaData() != null
					? new PropertySetBuilder(metric.getProperties()).createPropertySet()
					: null;
			switch (dataType) {
				case DataSet:
					this.value = metric.getValue() != null
							? new DataSetBuilder((DataSet) metric.getValue()).createDataSet()
							: null;
					break;
				case Template:
					this.value = metric.getValue() != null
							? new TemplateBuilder((Template) metric.getValue()).createTemplate()
							: null;
					break;
				default:
					this.value = metric.getValue();
			}
		}

		public MetricBuilder name(String name) {
			this.name = name;
			return this;
		}

		public MetricBuilder alias(Long alias) {
			this.alias = alias;
			return this;
		}

		public MetricBuilder timestamp(Date timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public MetricBuilder dataType(MetricDataType dataType) {
			this.dataType = dataType;
			return this;
		}

		public MetricBuilder isHistorical(Boolean isHistorical) {
			this.isHistorical = isHistorical;
			return this;
		}

		public MetricBuilder isTransient(Boolean isTransient) {
			this.isTransient = isTransient;
			return this;
		}

		public MetricBuilder metaData(MetaData metaData) {
			this.metaData = metaData;
			return this;
		}

		public MetricBuilder properties(PropertySet properties) {
			this.properties = properties;
			return this;
		}

		public MetricBuilder value(Object value) {
			this.value = value;
			return this;
		}

		public Metric createMetric() throws SparkplugInvalidTypeException {
			return new Metric(name, alias, timestamp, dataType, isHistorical, isTransient, metaData, properties, value);
		}
	}
}
