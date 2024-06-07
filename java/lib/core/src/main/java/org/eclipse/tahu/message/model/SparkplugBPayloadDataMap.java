/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2024 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.eclipse.tahu.message.model.SparkplugBPayloadMap.SparkplugBPayloadMapBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A class representing a Sparkplug B payload as a {@link Map} to prevent duplication of {@link Metric}s. This can be
 * useful for Sparkplug BIRTH payloads
 */
public class SparkplugBPayloadDataMap extends SparkplugBPayload {

	private static Logger logger = LoggerFactory.getLogger(SparkplugBPayloadDataMap.class.getName());

	private final Map<String, Metric> metricMap;

	private final Set<String> metricIdSet;

	private final Object mapLock = new Object();

	/**
	 * Default Constructor
	 */
	public SparkplugBPayloadDataMap() {
		super();
		metricMap = new ConcurrentSkipListMap<>();
		metricIdSet = ConcurrentHashMap.newKeySet();
	}

	/**
	 * Constructor
	 *
	 * @param timestamp the overall {@link Date} timestamp of the {@link SparkplugBPayload}
	 * @param metrics a {@link List} of {@link Metrics} in the {@link SparkplugBPayload}
	 * @param seq the Sparkplug sequence number for the {@link SparkplugBPayload}
	 * @param uuid a UUID for the {@link SparkplugBPayload}
	 * @param body an array of bytes for the {@link SparkplugBPayload}
	 */
	public SparkplugBPayloadDataMap(Date timestamp, List<Metric> metrics, long seq, String uuid, byte[] body) {
		super(timestamp, null, seq, uuid, body);
		metricMap = new ConcurrentSkipListMap<>();
		metricIdSet = ConcurrentHashMap.newKeySet();
		for (Metric metric : metrics) {
			try {
				metricMap.put(metric.getKey(), metric);
				metricIdSet.add(metric.hasName() ? metric.getName() : metric.getAlias().toString());
			} catch (Exception e) {
				logger.error("Failed to init Metric: {}", metric);
			}
		}
	}

	/**
	 * Adds a {@link Metric} to the {@link SparkplugBPayload}. If the {@link Metric} is already present with the same
	 * name, it will be replaced.
	 *
	 * metric the {@link Metric} to add
	 */
	@Override
	public void addMetric(Metric metric) {
		synchronized (mapLock) {
			try {
				if (logger.isDebugEnabled()) {
					if (metricMap.containsKey(metric.getKey())) {
						logger.debug("Metric key: {}", metric.getKey());
						logger.debug("\tOverwriting existing metric: {}", metricMap.get(metric.getKey()));
						logger.debug("\twith new metric: {}", metric);
					}
				}
				metricMap.put(metric.getKey(), metric);
				metricIdSet.add(metric.hasName() ? metric.getName() : metric.getAlias().toString());
			} catch (Exception e) {
				logger.error("Failed to add Metric: {}", metric);
			}
		}
	}

	/**
	 * Adds a {@link Metric} to the {@link SparkplugBPayload}. If the {@link Metric} is already present with the same
	 * name, it will be replaced.
	 *
	 * index this is ignored for this implementation metric the {@link Metric} to add
	 */
	@Override
	public void addMetric(int index, Metric metric) {
		synchronized (mapLock) {
			try {
				metricMap.put(metric.getKey(), metric);
				metricIdSet.add(metric.hasName() ? metric.getName() : metric.getAlias().toString());
			} catch (Exception e) {
				logger.error("Failed to init Metric at {}: {}", index, metric);
			}
		}
	}

	/**
	 * Adds a {@link List} of {@link Metric}s to the {@link SparkplugBPayloadMap}. If the list of {@link Metric}s has
	 * metrics with duplicate names, only the last one in the {@link List} will be included in the
	 * {@link SparkplugBPayloadMap}
	 *
	 * metrics a {@link List} of {@link Metric}s to add to the {@link SparkplugBPayloadMap}
	 */
	@Override
	public void addMetrics(List<Metric> metrics) {
		synchronized (mapLock) {
			for (Metric metric : metrics) {
				addMetric(metric);
			}
		}
	}

	/**
	 * Not used for the {@link SparkplugBPayloadMap}. This will always do nothing and return null.
	 *
	 * index not used
	 */
	@Override
	public Metric removeMetric(int index) {
		// This method isn't valid for the SparkplugBPayloadMap
		logger.error("removeMetric(int index) isn't supported by the SparkplugBPayloadDataMap");
		return null;
	}

	/**
	 * Removes a {@link Metric} by equality to a {@link Metric} in the {@link List} of metrics
	 *
	 * @param metric the {@link Metric} to remove
	 * @return true if the {@link Metric} was removed, otherwise false
	 */
	@Override
	public boolean removeMetric(Metric metric) {
		synchronized (mapLock) {
			boolean foundMetric = false;
			if (metric != null) {
				Iterator<String> it = metricMap.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (metric.hasName() && key.contains("_" + metric.getName())) {
						try {
							long timestamp = Long.parseLong(key.substring(0, key.indexOf("_")));
							// This is a match - remove it
							logger.debug("Removing metric: {}", metric);
							it.remove();
							foundMetric = true;
						} catch (Exception e) {
							logger.trace("This isn't the metric we are looking for with key={}: {}", key, metric);
							continue;
						}
					} else if (metric.hasAlias() && key.contains("_" + metric.getAlias())) {
						try {
							long timestamp = Long.parseLong(key.substring(0, key.indexOf("_")));
							// This is a match - remove it
							logger.debug("Removing metric: {}", metric);
							it.remove();
							foundMetric = true;
						} catch (Exception e) {
							logger.trace("This isn't the metric we are looking for with key={}: {}", key, metric);
							continue;
						}
					}
				}
			}

			return foundMetric;
		}
	}

	/**
	 * Gets a {@link List} of {@link Metric}s in the {@link SparkplugBPayloadMap}
	 */
	@Override
	public List<Metric> getMetrics() {
		return new ArrayList<>(metricMap.values());
	}

	/**
	 * Gets the number of {@link Metric}s in this {@link SparkplugBPayloadMap}
	 */
	@Override
	@JsonIgnore
	public Integer getMetricCount() {
		return metricMap.size();
	}

	/**
	 * Sets the {@link List} of {@link Metric}s for this {@link SparkplugBPayloadMap}
	 *
	 * @param metrics the {@link List} of {@link Metric}s to set for this {@link SparkplugBPayloadMap}
	 */
	@Override
	public void setMetrics(List<Metric> metrics) {
		metricMap.clear();
		for (Metric metric : metrics) {
			this.addMetric(metric);
		}
	}

	/**
	 * Returns the Metric Map
	 *
	 * @return the Metric Map
	 */
	@JsonIgnore
	public Map<String, Metric> getMetricMap() {
		return metricMap;
	}

	/**
	 * Returns the Metric Name or Alias Set
	 *
	 * @return the Metric Name or Alias Set
	 */
	@JsonIgnore
	public Set<String> getMetricIdSet() {
		return metricIdSet;
	}

	/**
	 * Updates all {@link Metric} timestamps to the specified {@link Date} as well as the timestamp for the overall
	 * {@link SparkplugBPayloadMap}
	 *
	 * @param date the {@link Date} timestamp to use for all {@link Metric}s in this {@link SparkplugBPayloadMap}
	 */
	public void updateMetricTimestamps(Date date) {
		for (Metric metric : metricMap.values()) {
			logger.debug("Updating metric timestamp for {} to {}", metric, date.getTime());
			metric.setTimestamp(date);
			if (metric.getDataType() == MetricDataType.Template && metric.getValue() != null) {
				updateTemplateTimestamps((Template) metric.getValue(), date);
			}
		}
	}

	private void updateTemplateTimestamps(Template template, Date date) {
		if (template != null && template.getMetrics() != null) {
			for (Metric metric : template.getMetrics()) {
				logger.debug("Updating metric timestamp for {} to {}", metric, date.getTime());
				metric.setTimestamp(date);
				if (metric.getDataType() == MetricDataType.Template && metric.getValue() != null) {
					updateTemplateTimestamps((Template) metric.getValue(), date);
				}
			}
		}
	}

	public void uptickMetricTimestamps(Date birthTimestamp) {
		for (Metric metric : metricMap.values()) {
			Date uptickedTimestamp = new Date(metric.getTimestamp().getTime() + 1);
			if (birthTimestamp.before(uptickedTimestamp)) {
				logger.debug("Updating metric timestamp for {} to birthTimestamp -> {}", metric,
						uptickedTimestamp.getTime());
				metric.setTimestamp(birthTimestamp);
			} else {
				logger.debug("Updating metric timestamp for {} to uptickedTimestamp -> {}", metric,
						uptickedTimestamp.getTime());
				metric.setTimestamp(uptickedTimestamp);
			}
			if (metric.getDataType() == MetricDataType.Template && metric.getValue() != null) {
				uptickTemplateTimestamps((Template) metric.getValue(), birthTimestamp);
			}
		}
	}

	private void uptickTemplateTimestamps(Template template, Date birthTimestamp) {
		if (template != null && template.getMetrics() != null) {
			for (Metric metric : template.getMetrics()) {
				Date uptickedTimestamp = new Date(metric.getTimestamp().getTime() + 1);
				if (birthTimestamp.before(uptickedTimestamp)) {
					logger.debug("Updating metric timestamp for {} to birthTimestamp -> {}", metric,
							uptickedTimestamp.getTime());
					metric.setTimestamp(birthTimestamp);
				} else {
					logger.debug("Updating metric timestamp for {} to uptickedTimestamp -> {}", metric,
							uptickedTimestamp.getTime());
					metric.setTimestamp(uptickedTimestamp);
				}
				if (metric.getDataType() == MetricDataType.Template && metric.getValue() != null) {
					updateTemplateTimestamps((Template) metric.getValue(), birthTimestamp);
				}
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparkplugBPayloadMap [timestamp=");
		builder.append(super.getTimestamp() != null ? super.getTimestamp().getTime() : "null");
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
	public static class SparkplugBPayloadDataMapBuilder {

		private Date timestamp;
		private List<Metric> metrics;
		private long seq = -1;
		private String uuid;
		private byte[] body;

		public SparkplugBPayloadDataMapBuilder(long sequenceNumber) {
			this.seq = sequenceNumber;
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadDataMapBuilder() {
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadDataMapBuilder addMetric(Metric metric) {
			this.metrics.add(metric);
			return this;
		}

		public SparkplugBPayloadDataMapBuilder addMetrics(Collection<Metric> metrics) {
			this.metrics.addAll(metrics);
			return this;
		}

		public SparkplugBPayloadDataMapBuilder setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public SparkplugBPayloadDataMapBuilder setSeq(long seq) {
			this.seq = seq;
			return this;
		}

		public SparkplugBPayloadDataMapBuilder setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public SparkplugBPayloadDataMapBuilder setBody(byte[] body) {
			this.body = body;
			return this;
		}

		public SparkplugBPayloadDataMap createPayload() {
			return new SparkplugBPayloadDataMap(timestamp, metrics, seq, uuid, body);
		}
	}
}
