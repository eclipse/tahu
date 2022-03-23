/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SparkplugBPayloadMap extends SparkplugBPayload {

	private static Logger logger = LoggerFactory.getLogger(SparkplugBPayloadMap.class.getName());

	private final ConcurrentHashMap<String, Metric> metricMap;

	private final Object mapLock = new Object();

	public SparkplugBPayloadMap() {
		super();
		metricMap = new ConcurrentHashMap<>();
	}

	public SparkplugBPayloadMap(Date timestamp, List<Metric> metrics, long seq, String uuid, byte[] body) {
		super(timestamp, null, seq, uuid, body);
		metricMap = new ConcurrentHashMap<>();
		for (Metric metric : metrics) {
			metricMap.put(metric.getName(), metric);
		}
	}

	@Override
	public void addMetric(Metric metric) {
		synchronized (mapLock) {
			metricMap.put(metric.getName(), metric);
		}
	}

	@Override
	public void addMetric(int index, Metric metric) {
		synchronized (mapLock) {
			metricMap.put(metric.getName(), metric);
		}
	}

	@Override
	public void addMetrics(List<Metric> metrics) {
		synchronized (mapLock) {
			for (Metric metric : metrics) {
				addMetric(metric);
			}
		}
	}

	@Override
	public Metric removeMetric(int index) {
		// This method isn't valid for the SparkplugBPayloadMap
		logger.error("removeMetric(int index) isn't supported by the SparkplugBPayloadMap");
		return null;
	}

	@Override
	public boolean removeMetric(Metric metric) {
		synchronized (mapLock) {
			if (metric != null) {
				Metric removedMetric = metricMap.remove(metric.getName());
				if (removedMetric != null) {
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public List<Metric> getMetrics() {
		return new ArrayList<>(metricMap.values());
	}

	@Override
	@JsonIgnore
	public Integer getMetricCount() {
		return metricMap.size();
	}

	@Override
	public void setMetrics(List<Metric> metrics) {
		metricMap.clear();
		for (Metric metric : metrics) {
			metricMap.put(metric.getName(), metric);
		}
	}

	/**
	 * Updates a {@link Metric} value in the {@link SparkplugBPayloadMap}
	 * 
	 * @param metricName the name of the metric. This is required as Aliasing may be enabled and the name may not be set
	 *            in the {@link Metric}
	 * @param metric the {@link Metric} to update the value of
	 */
	public void updateMetricValue(String metricName, Metric metric) {
		if (metric == null) {
			logger.info("Metric '{}' is null during update - removing from cache", metricName);
			metricMap.put(metricName, null);
			return;
		}

		Metric existingMetric = metricMap.get(metricName);

		// Update the 'qualified value' which is the value, quality, and timestamp
		if (existingMetric != null) {
			existingMetric.setValue(metric.getValue());
			PropertySet props = existingMetric.getProperties();
			if (metric.getProperties() != null
					&& metric.getProperties().getPropertyValue(SparkplugMeta.QUALITY_PROP_NAME) != null) {
				props.setProperty(SparkplugMeta.QUALITY_PROP_NAME,
						metric.getProperties().getPropertyValue(SparkplugMeta.QUALITY_PROP_NAME));
			}
			existingMetric.setTimestamp(metric.getTimestamp());
		} else {
			logger.info("Adding new metric to cache when updating: {}", metricName);
			metricMap.put(metricName, metric);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparkplugBPayload [timestamp=");
		builder.append(super.getTimestamp());
		builder.append(", metrics=");
		builder.append(getMetrics());
		builder.append(", seq=");
		builder.append(super.getSeq());
		builder.append(", uuid=");
		builder.append(super.getUuid());
		builder.append(", body=");
		builder.append(Arrays.toString(super.getBody()));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link SparkplugBPayloadMapBuilder} instance.
	 */
	public static class SparkplugBPayloadMapBuilder {

		private Date timestamp;
		private List<Metric> metrics;
		private long seq = -1;
		private String uuid;
		private byte[] body;

		public SparkplugBPayloadMapBuilder(long sequenceNumber) {
			this.seq = sequenceNumber;
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadMapBuilder() {
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadMapBuilder addMetric(Metric metric) {
			this.metrics.add(metric);
			return this;
		}

		public SparkplugBPayloadMapBuilder addMetrics(Collection<Metric> metrics) {
			this.metrics.addAll(metrics);
			return this;
		}

		public SparkplugBPayloadMapBuilder setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public SparkplugBPayloadMapBuilder setSeq(long seq) {
			this.seq = seq;
			return this;
		}

		public SparkplugBPayloadMapBuilder setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public SparkplugBPayloadMapBuilder setBody(byte[] body) {
			this.body = body;
			return this;
		}

		public SparkplugBPayloadMap createPayload() {
			return new SparkplugBPayloadMap(timestamp, metrics, seq, uuid, body);
		}
	}
}
