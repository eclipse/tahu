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

import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A class representing a template associated with a metric
 */
@JsonInclude(Include.NON_NULL)
public class Template {

	/**
	 * The Template version.
	 */
	@JsonProperty("version")
	private String version;

	/**
	 * The template reference
	 */
	@JsonProperty("reference")
	private String templateRef;

	/**
	 * True if the template is a definition, false otherwise.
	 */
	@JsonProperty("isDefinition")
	private boolean isDefinition;

	/**
	 * List of metrics.
	 */
	@JsonProperty("metrics")
	private List<Metric> metrics;

	/**
	 * List of parameters.
	 */
	@JsonProperty("parameters")
	@JsonInclude(Include.NON_EMPTY)
	private List<Parameter> parameters;

	public Template() {
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
	public Template(String version, String templateRef, boolean isDefinition, List<Metric> metrics,
			List<Parameter> parameters) {
		this.version = version;
		this.templateRef = templateRef;
		this.isDefinition = isDefinition;
		this.metrics = metrics;
		this.parameters = parameters;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getTemplateRef() {
		return templateRef;
	}

	public void setTemplateRef(String templateRef) {
		this.templateRef = templateRef;
	}

	@JsonGetter("isDefinition")
	public boolean isDefinition() {
		return isDefinition;
	}

	@JsonSetter("isDefinition")
	public void setDefinition(boolean isDefinition) {
		this.isDefinition = isDefinition;
	}

	public List<Metric> getMetrics() {
		return metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(Metric metric) {
		this.metrics.add(metric);
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Template [version=");
		builder.append(version);
		builder.append(", templateRef=");
		builder.append(templateRef);
		builder.append(", isDefinition=");
		builder.append(isDefinition);
		builder.append(", metrics=");
		builder.append(metrics);
		builder.append(", parameters=");
		builder.append(parameters);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link Template} instance.
	 */
	public static class TemplateBuilder {

		private String version;
		private String templateRef;
		private boolean isDefinition;
		private List<Metric> metrics;
		private List<Parameter> parameters;

		/**
		 * @param name
		 * @param version
		 * @param templateRef
		 * @param isDefinition
		 * @param metrics
		 * @param parameters
		 */
		public TemplateBuilder() {
			super();
			this.metrics = new ArrayList<Metric>();
			this.parameters = new ArrayList<Parameter>();
		}

		public TemplateBuilder(Template template) throws SparkplugException {
			this.version = template.getVersion();
			this.templateRef = template.getTemplateRef();
			this.isDefinition = template.isDefinition();
			this.metrics = new ArrayList<Metric>(template.getMetrics().size());
			for (Metric metric : template.getMetrics()) {
				this.metrics.add(new MetricBuilder(metric).createMetric());
			}
			this.parameters = new ArrayList<Parameter>(template.getParameters().size());
			for (Parameter parameter : template.getParameters()) {
				this.parameters.add(new Parameter(parameter.getName(), parameter.getType(), parameter.getValue()));
			}
		}

		public TemplateBuilder version(String version) {
			this.version = version;
			return this;
		}

		public TemplateBuilder templateRef(String templateRef) {
			this.templateRef = templateRef;
			return this;
		}

		public TemplateBuilder definition(boolean isDefinition) {
			this.isDefinition = isDefinition;
			return this;
		}

		public TemplateBuilder addMetric(Metric metric) {
			this.metrics.add(metric);
			return this;
		}

		public TemplateBuilder addMetrics(Collection<Metric> metrics) {
			this.metrics.addAll(metrics);
			return this;
		}

		public TemplateBuilder addParameter(Parameter parameter) {
			this.parameters.add(parameter);
			return this;
		}

		public TemplateBuilder addParameters(Collection<Parameter> parameters) {
			this.parameters.addAll(parameters);
			return this;
		}

		public Template createTemplate() {
			return new Template(version, templateRef, isDefinition, metrics, parameters);
		}
	}
}
