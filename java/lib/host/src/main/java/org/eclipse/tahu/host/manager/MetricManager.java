/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host.manager;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.host.model.HostMetric;
import org.eclipse.tahu.message.model.SparkplugDescriptor;

public abstract class MetricManager {

	private final Map<String, HostMetric> metricMap;

	public MetricManager() {
		metricMap = new ConcurrentHashMap<>();
	}

	public abstract SparkplugDescriptor getSparkplugDescriptor();

	public Map<String, HostMetric> getMetricMap() {
		return Collections.unmodifiableMap(metricMap);
	}

	public Set<String> getMetricNames() {
		return metricMap.keySet();
	}

	public HostMetric getMetric(String metricName) {
		return metricMap.get(metricName);
	}

	public void putMetric(String metricName, HostMetric metric) {
		metricMap.put(metricName, metric);
	}

	public void updateValue(String metricName, Object value) {
		metricMap.get(metricName).setValue(value);
	}

	public void setStale(String metricName, boolean stale) {
		metricMap.get(metricName).setStale(stale);
	}

	public void clearMetrics() {
		metricMap.clear();
	}
}
