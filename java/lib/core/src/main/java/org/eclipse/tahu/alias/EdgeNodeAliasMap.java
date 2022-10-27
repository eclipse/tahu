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
