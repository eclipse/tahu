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

package org.eclipse.tahu.host.alias;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tahu.alias.EdgeNodeAliasMap;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;

public class HostApplicationAliasMap {

	private static HostApplicationAliasMap instance;

	private final Map<EdgeNodeDescriptor, EdgeNodeAliasMap> edgeNodeAliasMaps;

	private final Object mapLock = new Object();

	public static HostApplicationAliasMap getInstance() {
		if (instance == null) {
			instance = new HostApplicationAliasMap();
		}
		return instance;
	}

	private HostApplicationAliasMap() {
		edgeNodeAliasMaps = new ConcurrentHashMap<>();
	}

	public void addAlias(EdgeNodeDescriptor edgeNodeDescriptor, String metricName, Long alias) {
		synchronized (mapLock) {
			EdgeNodeAliasMap edgeNodeAliasMap =
					edgeNodeAliasMaps.computeIfAbsent(edgeNodeDescriptor, (k) -> new EdgeNodeAliasMap());
			edgeNodeAliasMap.addAlias(metricName, alias);
		}
	}

	public void clear() {
		synchronized (mapLock) {
			edgeNodeAliasMaps.clear();
		}
	}

	public Long getAlias(EdgeNodeDescriptor edgeNodeDescriptor, String metricName) {
		EdgeNodeAliasMap edgeNodeAliasMap = edgeNodeAliasMaps.get(edgeNodeDescriptor);
		if (edgeNodeAliasMap != null) {
			return edgeNodeAliasMap.getAlias(metricName);
		} else {
			return null;
		}
	}

	public String getMetricName(EdgeNodeDescriptor edgeNodeDescriptor, long alias) {
		EdgeNodeAliasMap edgeNodeAliasMap = edgeNodeAliasMaps.get(edgeNodeDescriptor);
		if (edgeNodeAliasMap != null) {
			return edgeNodeAliasMap.getMetricName(alias);
		} else {
			return null;
		}
	}

	public boolean aliasExists(EdgeNodeDescriptor edgeNodeDescriptor, long alias) {
		EdgeNodeAliasMap edgeNodeAliasMap = edgeNodeAliasMaps.get(edgeNodeDescriptor);
		if (edgeNodeAliasMap != null && edgeNodeAliasMap.getMetricName(alias) != null) {
			return true;
		} else {
			return false;
		}
	}
}
