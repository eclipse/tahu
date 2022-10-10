/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.alias;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EdgeNodeAliasMap {

	private static Logger logger = LoggerFactory.getLogger(EdgeNodeAliasMap.class.getName());

	private final Map<String, Long> metricNameToAliasMap;
	private final Map<Long, String> aliasToMetricNameMap;

	private long nextAliasIndex;

	private final Object mapLock = new Object();

	public EdgeNodeAliasMap() {
		metricNameToAliasMap = new ConcurrentHashMap<>();
		aliasToMetricNameMap = new ConcurrentHashMap<>();
		nextAliasIndex = 0;
	}

	public long addGeneratedAlias(String metricName) {
		synchronized (mapLock) {
			long newAlias = nextAliasIndex++;
			metricNameToAliasMap.put(metricName, newAlias);
			aliasToMetricNameMap.put(newAlias, metricName);
			return newAlias;
		}
	}

	public void addAlias(String metricName, Long alias) {
		synchronized (mapLock) {
			metricNameToAliasMap.put(metricName, alias);
			aliasToMetricNameMap.put(alias, metricName);
		}
	}

	public void clear() {
		synchronized (mapLock) {
			metricNameToAliasMap.clear();
			aliasToMetricNameMap.clear();
			nextAliasIndex = 0;
		}
	}

	public Long getAlias(String metricName) {
		return metricNameToAliasMap.get(metricName);
	}

	public String getMetricName(long alias) {
		return aliasToMetricNameMap.get(alias);
	}
}
