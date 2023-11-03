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
		HostMetric hostMetric = metricMap.get(metricName);
		if (hostMetric != null) {
			hostMetric.setValue(value);
		}
	}

	public void setStale(String metricName, boolean stale) {
		HostMetric hostMetric = metricMap.get(metricName);
		if (hostMetric != null) {
			hostMetric.setStale(stale);
		}
	}

	public void clearMetrics() {
		metricMap.clear();
	}
}
