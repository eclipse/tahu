/********************************************************************************
 * Copyright (c) 2023 Cirrus Link Solutions and others
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

package org.eclipse.tahu.edge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.model.MetricDataTypeMap;
import org.eclipse.tahu.model.MetricMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeNodeMetricMaps {

	private static Logger logger = LoggerFactory.getLogger(EdgeNodeMetricMaps.class.getName());

	private static Map<String, EdgeNodeMetricMaps> instances;

	private final Map<EdgeNodeDescriptor, Map<SparkplugDescriptor, MetricMap>> allEdgeNodeMetricMaps;

	private final Object mapLock = new Object();

	public static EdgeNodeMetricMaps getInstance(String agentName) {
		if (instances == null) {
			instances = new ConcurrentHashMap<>();
		}
		if (instances.get(agentName) == null) {
			instances.put(agentName, new EdgeNodeMetricMaps());
		}
		return instances.get(agentName);
	}

	private EdgeNodeMetricMaps() {
		allEdgeNodeMetricMaps = new ConcurrentHashMap<>();
	}

	public void addMetric(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			String metricName, Metric metric) {
		synchronized (mapLock) {
			Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps =
					allEdgeNodeMetricMaps.computeIfAbsent(edgeNodeDescriptor, (k) -> new ConcurrentHashMap<>());
			MetricMap metricMap = edgeNodeMetricMaps.computeIfAbsent(sparkplugDescriptor, (k) -> new MetricMap());
			metricMap.addAlias(metricName, metric.getAlias(), metric.getDataType());

			if (metric.getDataType() == MetricDataType.Template && metric.getValue() != null
					&& Template.class.isAssignableFrom(metric.getValue().getClass())) {
				Template template = (Template) metric.getValue();
				for (Metric childMetric : template.getMetrics()) {
					addMetric(edgeNodeDescriptor, sparkplugDescriptor, metricName + "/" + childMetric.getName(),
							childMetric);
				}
			}
		}
	}

	public void clear() {
		synchronized (mapLock) {
			allEdgeNodeMetricMaps.clear();
		}
	}

	public Long getAlias(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			String metricName) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null) {
			MetricMap metricMap = edgeNodeMetricMaps.get(sparkplugDescriptor);
			if (metricMap != null) {
				return metricMap.getAlias(metricName);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public String getMetricName(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			long alias) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null) {
			MetricMap metricMap = edgeNodeMetricMaps.get(sparkplugDescriptor);
			if (metricMap != null) {
				return metricMap.getMetricName(alias);
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public boolean aliasExists(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			long alias) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null && edgeNodeMetricMaps.get(sparkplugDescriptor) != null) {
			MetricMap metricMap = edgeNodeMetricMaps.get(sparkplugDescriptor);
			if (metricMap != null && metricMap.getMetricName(alias) != null) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public MetricDataTypeMap getMetricDataTypeMap(EdgeNodeDescriptor edgeNodeDescriptor,
			SparkplugDescriptor sparkplugDescriptor) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null && edgeNodeMetricMaps.get(sparkplugDescriptor) != null) {
			return edgeNodeMetricMaps.get(sparkplugDescriptor).getMetricDataTypeMap();
		} else {
			return null;
		}
	}

	public MetricDataType getDataType(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			String metricName) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null && edgeNodeMetricMaps.get(sparkplugDescriptor) != null) {
			return edgeNodeMetricMaps.get(sparkplugDescriptor).getMetricDataType(metricName);
		} else {
			return null;
		}
	}

	public MetricDataType getDataType(EdgeNodeDescriptor edgeNodeDescriptor, SparkplugDescriptor sparkplugDescriptor,
			Long alias) {
		Map<SparkplugDescriptor, MetricMap> edgeNodeMetricMaps = allEdgeNodeMetricMaps.get(edgeNodeDescriptor);
		if (edgeNodeMetricMaps != null && edgeNodeMetricMaps.get(sparkplugDescriptor) != null) {
			return edgeNodeMetricMaps.get(sparkplugDescriptor).getMetricDataType(alias);
		} else {
			return null;
		}
	}
}
