/********************************************************************************
 * Copyright (c) 2022 Cirrus Link Solutions and others
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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.DataSet.DataSetBuilder;
import org.eclipse.tahu.message.model.MetaData.MetaDataBuilder;
import org.eclipse.tahu.message.model.PropertySet.PropertySetBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class representing a {@link Map} of {@link Template} {@link Metric}s
 */
public class TemplateMap extends Template {

	private static Logger logger = LoggerFactory.getLogger(TemplateMap.class.getName());

	private final Map<String, Metric> metricMap;

	private final Object mapLock = new Object();

	public TemplateMap() {
		metricMap = new ConcurrentHashMap<>();
	}

	/**
	 * Constructor
	 * 
	 * @param name the template name
	 * @param version the template version
	 * @param templateRef a template reference
	 * @param isDefinition a flag indicating if this is a template definition
	 * @param metrics a list of metrics
	 * @param parmeters a list of parameters
	 */
	public TemplateMap(String version, String templateRef, boolean isDefinition, Map<String, Metric> metricMap,
			List<Parameter> parameters) {
		super(version, templateRef, isDefinition, null, parameters);
		this.metricMap = metricMap;
	}

	/**
	 * Gets an unmodifiable {@link Map} of the {@link Metric}s
	 *
	 * @return an unmodifiable {@link Map} of the {@link Metric}s
	 */
	public Map<String, Metric> getMetricMap() {
		synchronized (mapLock) {
			return Collections.unmodifiableMap(metricMap);
		}
	}

	/**
	 * Updates a {@link Metric} in the {@link TemplateMap}
	 *
	 * @param metricName the name of the {@link Metric} to update
	 * @param metric the {@link Metric} to place in the {@link Map}
	 */
	public void updateMetric(String metricName, Metric metric) {
		synchronized (mapLock) {
			metricMap.put(metricName, metric);
		}
	}

	/**
	 * Gets the {@link List} of {@link Metric}s in this {@link TemplateMap}
	 */
	@Override
	public List<Metric> getMetrics() {
		synchronized (mapLock) {
			return new ArrayList<>(metricMap.values());
		}
	}

	/**
	 * Sets the {@link Map} of {@link Metric}s using a {@link List}. If any metric names are duplicated in the list,
	 * only the last one will be placed in the map.
	 */
	@Override
	public void setMetrics(List<Metric> metrics) {
		synchronized (mapLock) {
			for (Metric metric : metrics) {
				addMetric(metric);
			}
		}
	}

	/**
	 * Adds an {@link Metric} to the end of the {@link TemplateMap}
	 *
	 * @param metric the {@link Metric} to add to the {@link TemplateMap}
	 */
	@Override
	public void addMetric(Metric metric) {
		synchronized (mapLock) {
			metricMap.put(metric.getName(), metric);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemplateMap [version=");
		builder.append(super.getVersion());
		builder.append(", templateRef=");
		builder.append(super.getTemplateRef());
		builder.append(", isDefinition=");
		builder.append(super.isDefinition());
		builder.append(", metrics=");
		builder.append(metricMap);
		builder.append(", parameters=");
		builder.append(super.getParameters());
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link TemplateMap} instance.
	 */
	public static class TemplateMapBuilder {

		private String version;
		private String templateRef;
		private boolean isDefinition;
		private Map<String, Metric> metricMap;
		private List<Parameter> parameters;

		/**
		 * @param name
		 * @param version
		 * @param templateRef
		 * @param isDefinition
		 * @param metrics
		 * @param parameters
		 */
		public TemplateMapBuilder() {
			super();
			this.metricMap = new ConcurrentHashMap<>();
			this.parameters = new ArrayList<>();
		}

		public TemplateMapBuilder(Template template) throws SparkplugException {
			this.version = template.getVersion();
			this.templateRef = template.getTemplateRef();
			this.isDefinition = template.isDefinition();

			this.metricMap = new ConcurrentHashMap<>(template.getMetrics().size());
			for (Metric metric : template.getMetrics()) {
				logger.trace("Adding metric '{}' when converting Template to TemplateMap", metric.getName());
				this.metricMap.put(metric.getName(), new CustomMetricBuilder(metric).createMetric());
			}
			logger.trace("MetricMap after conversion: {}", metricMap);
			this.parameters = new ArrayList<Parameter>(template.getParameters().size());
			for (Parameter parameter : template.getParameters()) {
				this.parameters.add(new Parameter(parameter.getName(), parameter.getType(), parameter.getValue()));
			}
		}

		public TemplateMapBuilder(TemplateMap templateMap) throws SparkplugException {
			this.version = templateMap.getVersion();
			this.templateRef = templateMap.getTemplateRef();
			this.isDefinition = templateMap.isDefinition();
			this.metricMap = new ConcurrentHashMap<>(templateMap.getMetrics().size());
			for (Metric metric : templateMap.getMetrics()) {
				this.metricMap.put(metric.getName(), new CustomMetricBuilder(metric).createMetric());
			}
			this.parameters = new ArrayList<Parameter>(templateMap.getParameters().size());
			for (Parameter parameter : templateMap.getParameters()) {
				this.parameters.add(new Parameter(parameter.getName(), parameter.getType(), parameter.getValue()));
			}
		}

		public TemplateMapBuilder version(String version) {
			this.version = version;
			return this;
		}

		public TemplateMapBuilder templateRef(String templateRef) {
			this.templateRef = templateRef;
			return this;
		}

		public TemplateMapBuilder definition(boolean isDefinition) {
			this.isDefinition = isDefinition;
			return this;
		}

		public TemplateMapBuilder addParameter(Parameter parameter) {
			this.parameters.add(parameter);
			return this;
		}

		public TemplateMapBuilder addParameters(Collection<Parameter> parameters) {
			this.parameters.addAll(parameters);
			return this;
		}

		public TemplateMap createTemplateMap() {
			return new TemplateMap(version, templateRef, isDefinition, metricMap, parameters);
		}
	}

	/**
	 * A builder for creating a {@link Metric} instance.
	 */
	public static class CustomMetricBuilder {

		private String name;
		private Long alias;
		private Date timestamp;
		private MetricDataType dataType;
		private Boolean isHistorical;
		private Boolean isTransient;
		private MetaData metaData = null;
		private PropertySet properties = null;
		private Object value;

		public CustomMetricBuilder(Metric metric) throws SparkplugException {
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
							? new TemplateMapBuilder((TemplateMap) metric.getValue()).createTemplateMap()
							: null;
					break;
				default:
					this.value = metric.getValue();
			}
		}

		public Metric createMetric() throws SparkplugInvalidTypeException {
			return new Metric(name, alias, timestamp, dataType, isHistorical, isTransient, metaData, properties, value);
		}
	}
}
