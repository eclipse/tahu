/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
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
