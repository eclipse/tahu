/********************************************************************************
 * Copyright (c) 2014-2022 Cirrus Link Solutions and others
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

package org.eclipse.tahu.message.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A class representing a Sparkplug B payload
 */
@JsonInclude(Include.NON_NULL)
public class SparkplugBPayload {

	private static Logger logger = LoggerFactory.getLogger(SparkplugBPayload.class.getName());

	private Date timestamp;
	private List<Metric> metrics;
	private Long seq = null;
	private String uuid;
	private byte[] body;

	/**
	 * Default Constructor
	 */
	public SparkplugBPayload() {
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
	public SparkplugBPayload(Date timestamp, List<Metric> metrics, Long seq, String uuid, byte[] body) {
		this(timestamp, metrics, seq);
		this.uuid = uuid;
		this.body = body;
	}

	/**
	 * Constructor
	 *
	 * @param timestamp the overall {@link Date} timestamp of the {@link SparkplugBPayload}
	 * @param metrics a {@link List} of {@link Metrics} in the {@link SparkplugBPayload}
	 * @param seq the Sparkplug sequence number for the {@link SparkplugBPayload}
	 */
	public SparkplugBPayload(Date timestamp, List<Metric> metrics, Long seq) {
		this(timestamp, metrics);
		this.seq = seq;
	}

	/**
	 * Constructor
	 *
	 * @param timestamp the overall {@link Date} timestamp of the {@link SparkplugBPayload}
	 * @param metrics a {@link List} of {@link Metrics} in the {@link SparkplugBPayload}
	 */
	public SparkplugBPayload(Date timestamp, List<Metric> metrics) {
		this.timestamp = timestamp;
		this.metrics = metrics;
	}

	/**
	 * Copy Constructor
	 *
	 * @param payload the {@link SparkplugBPayload} to copy
	 */
	public SparkplugBPayload(SparkplugBPayload payload) {
		this.timestamp = payload.getTimestamp();
		if (payload.getMetrics() != null) {
			metrics = new ArrayList<>();
			for (Metric metric : payload.getMetrics()) {
				try {
					metrics.add(new Metric(metric));
				} catch (Exception e) {
					logger.error("Failed to copy metric: {}", metric, e);
				}
			}
		}
		this.seq = payload.getSeq();
		this.uuid = payload.getUuid();
		this.body = payload.getBody();
	}

	/**
	 * Gets the timestamp of the {@link SparkplugBPayload} as a {@link Date}
	 *
	 * @return a {@link Date} representing the timestamp of the {@link SparkplugBPayload}
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Sets the timestamp of the {@link SparkplugBPayload}
	 *
	 * @param timestamp the {@link Date} timestamp to set for the {@link SparkplugBPayload}
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Adds a {@link Metric} to the end of the {@link List} of Sparkplug metrics
	 *
	 * @param metric a {@link Metric} to add to the end of the {@link List} of Sparkplug metrics
	 */
	public void addMetric(Metric metric) {
		metrics.add(metric);
	}

	/**
	 * Adds a {@link Metric} at the specified index to the {@link List} of Sparkplug metrics
	 *
	 * @param index the index to use in the {@link List} of {@link Metric}s when adding the {@link Metric}
	 * @param metric a {@link Metric} to add at the specified index to the {@link List} of Sparkplug metrics
	 */
	public void addMetric(int index, Metric metric) {
		metrics.add(index, metric);
	}

	/**
	 * Sets the {@link List} of {@link Metric}s for the {@link SparkplugBPayload}
	 *
	 * @param metrics the {@link List} of {@link Metric}s to set for the {@link SparkplugBPayload}
	 */
	public void addMetrics(List<Metric> metrics) {
		this.metrics.addAll(metrics);
	}

	/**
	 * Removes a {@link Metric} from the {@link List} of {@link Metric}s in the {@link SparkplugBPayload}
	 *
	 * @param index the index to use when removing the {@link Metric}
	 * @return the {@link Metric} that was removed
	 */
	public Metric removeMetric(int index) {
		return metrics.remove(index);
	}

	/**
	 * Removes a {@link Metric} by equality to a {@link Metric} in the {@link List} of metrics
	 *
	 * @param metric the {@link Metric} to remove
	 * @return true if the {@link Metric} was removed, otherwise false
	 */
	public boolean removeMetric(Metric metric) {
		return metrics.remove(metric);
	}

	/**
	 * Gets the {@link List} of {@link Metric}s associated with the {@link SparkplugBPayload}
	 *
	 * @return the {@link List} of {@link Metric}s associated with the {@link SparkplugBPayload}
	 */
	public List<Metric> getMetrics() {
		return metrics;
	}

	/**
	 * Gets the number of {@link Metric}s in this {@link SparkplugBPayload}
	 *
	 * @return the number of {@link Metric}s in this {@link SparkplugBPayload}
	 */
	@JsonIgnore
	public Integer getMetricCount() {
		return metrics.size();
	}

	/**
	 * Sets the {@link List} of {@link Metric}s for this {@link SparkplugBPayload}
	 *
	 * @param metrics the {@link List} of {@link Metric}s to set for this {@link SparkplugBPayload}
	 */
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	/**
	 * Gets the sequence number for this {@link SparkplugBPayload}
	 *
	 * @return the sequence number for this {@link SparkplugBPayload}
	 */
	public Long getSeq() {
		return seq;
	}

	/**
	 * Sets the sequence number for this {@link SparkplugBPayload}
	 *
	 * @param seq the sequence number to set for this {@link SparkplugBPayload}
	 */
	public void setSeq(Long seq) {
		this.seq = seq;
	}

	/**
	 * Gets the UUID for this {@link SparkplugBPayload}
	 *
	 * @return the UUID for this {@link SparkplugBPayload}
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the UUID for this {@link SparkplugBPayload}
	 *
	 * @param seq the UUID to set for this {@link SparkplugBPayload}
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the body for this {@link SparkplugBPayload}
	 *
	 * @return the body for this {@link SparkplugBPayload}
	 */
	public byte[] getBody() {
		return body;
	}

	/**
	 * Sets the body for this {@link SparkplugBPayload}
	 *
	 * @param seq the body to set for this {@link SparkplugBPayload}
	 */
	public void setBody(byte[] body) {
		this.body = body;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SparkplugBPayload [timestamp=");
		builder.append(timestamp != null ? timestamp.getTime() : "null");
		builder.append(", metrics=");
		builder.append(metrics);
		builder.append(", seq=");
		builder.append(seq != null ? seq : "null");
		builder.append(", uuid=");
		builder.append(uuid);
		builder.append(", body=");
		builder.append(Arrays.toString(body));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link SparkplugBPayload} instance.
	 */
	public static class SparkplugBPayloadBuilder {

		private Date timestamp;
		private List<Metric> metrics;
		private Long seq = null;
		private String uuid;
		private byte[] body;

		public SparkplugBPayloadBuilder(Long sequenceNumber) {
			this.seq = sequenceNumber;
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadBuilder() {
			metrics = new ArrayList<Metric>();
		}

		public SparkplugBPayloadBuilder addMetric(Metric metric) {
			this.metrics.add(metric);
			return this;
		}

		public SparkplugBPayloadBuilder addMetrics(Collection<Metric> metrics) {
			this.metrics.addAll(metrics);
			return this;
		}

		public SparkplugBPayloadBuilder setTimestamp(Date timestamp) {
			this.timestamp = timestamp;
			return this;
		}

		public SparkplugBPayloadBuilder setSeq(Long seq) {
			this.seq = seq;
			return this;
		}

		public SparkplugBPayloadBuilder setUuid(String uuid) {
			this.uuid = uuid;
			return this;
		}

		public SparkplugBPayloadBuilder setBody(byte[] body) {
			this.body = body;
			return this;
		}

		public SparkplugBPayload createPayload() {
			return new SparkplugBPayload(timestamp, metrics, seq, uuid, body);
		}
	}
}
