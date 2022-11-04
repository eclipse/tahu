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

package org.eclipse.tahu.alias;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to track Sparkplug aliases to Metric names and Metric names to aliases
 */
public class EdgeNodeAliasMap {

	private final Map<String, Long> metricNameToAliasMap;
	private final Map<Long, String> aliasToMetricNameMap;

	private long nextAliasIndex;

	private final Object mapLock = new Object();

	/**
	 * Constructor
	 */
	public EdgeNodeAliasMap() {
		metricNameToAliasMap = new ConcurrentHashMap<>();
		aliasToMetricNameMap = new ConcurrentHashMap<>();
		nextAliasIndex = 0;
	}

	/**
	 * Addes a new metric to the map and generates and returns the new alias that will be unique as required for the
	 * Edge Node
	 *
	 * @param metricName the name of the metric to generate the alias for
	 *
	 * @return the generated alias for the supplied Metric name
	 */
	public long addGeneratedAlias(String metricName) {
		synchronized (mapLock) {
			long newAlias = nextAliasIndex++;
			metricNameToAliasMap.put(metricName, newAlias);
			aliasToMetricNameMap.put(newAlias, metricName);
			return newAlias;
		}
	}

	/**
	 * Adds a Metric name and alias to the map. The alias must be unique and not already tied to another Metric name
	 * before calling this method
	 *
	 * @param metricName the name of the Metric to add to the map
	 * @param alias the alias to add to the map and be tied to the alias
	 */
	public void addAlias(String metricName, Long alias) {
		synchronized (mapLock) {
			metricNameToAliasMap.put(metricName, alias);
			aliasToMetricNameMap.put(alias, metricName);
		}
	}

	/**
	 * Clears the map of all Metric names and aliases
	 */
	public void clear() {
		synchronized (mapLock) {
			metricNameToAliasMap.clear();
			aliasToMetricNameMap.clear();
			nextAliasIndex = 0;
		}
	}

	/**
	 * Gets and alias for a given Metric name
	 *
	 * @param metricName the Metric name associated with the alias
	 * @return the alias that is associated with the Metric name
	 */
	public Long getAlias(String metricName) {
		return metricNameToAliasMap.get(metricName);
	}

	/**
	 * Gets and Metric name for a given alias
	 *
	 * @param alias the alias associated with the Metric name
	 * @return the alias that is associated with the Metric name
	 */
	public String getMetricName(long alias) {
		return aliasToMetricNameMap.get(alias);
	}
}
