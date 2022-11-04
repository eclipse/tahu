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

package org.eclipse.tahu.host.model;

import java.util.Date;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.model.MetaData;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.PropertySet;

public class HostMetric extends Metric {

	private boolean stale;

	public HostMetric(boolean stale) {
		super();
		this.stale = stale;
	}

	public HostMetric(String name, Long alias, Date timestamp, MetricDataType dataType, Boolean isHistorical,
			Boolean isTransient, MetaData metaData, PropertySet properties, Object value, boolean stale)
			throws SparkplugInvalidTypeException {
		super(name, alias, timestamp, dataType, isHistorical, isTransient, metaData, properties, value);
		this.stale = stale;
	}

	public HostMetric(Metric metric, boolean stale) throws SparkplugInvalidTypeException {
		this(metric.getName(), metric.getAlias(), metric.getTimestamp(), metric.getDataType(), metric.isHistorical(),
				metric.isTransient(), metric.getMetaData(), metric.getProperties(), metric.getValue(), stale);
	}

	public boolean isStale() {
		return stale;
	}

	public void setStale(boolean stale) {
		this.stale = stale;
	}
}
