/********************************************************************************
 * Copyright (c) 2016, 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.util.CompressionAlgorithm;
import org.eclipse.tahu.util.PayloadUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for PayloadUtil.
 */
public class PayloadUtilTest {

	private Date testTime;

	public PayloadUtilTest() {
		this.testTime = new Date();
	}

	@BeforeClass
	public void beforeClass() {
		Logger rootLogger = Logger.getRootLogger();
		rootLogger.setLevel(Level.ALL);
		rootLogger.addAppender(new ConsoleAppender(new PatternLayout("%-6r [%p] %c - %m%n")));
	}

	@DataProvider
	public Object[][] compressionData() throws Exception {
		return new Object[][] {
				{ CompressionAlgorithm.DEFLATE,
						new SparkplugBPayloadBuilder().setTimestamp(testTime).setSeq(0).setUuid("123456789")
								.setBody("Hello".getBytes())
								.addMetric(
										new MetricBuilder("TestInt", MetricDataType.Int32, 1234567890).createMetric())
								.createPayload() },
				{ CompressionAlgorithm.GZIP,
						new SparkplugBPayloadBuilder().setTimestamp(testTime).setSeq(0).setUuid("123456789")
								.setBody("Hello".getBytes())
								.addMetric(
										new MetricBuilder("TestInt", MetricDataType.Int32, 1234567890).createMetric())
								.createPayload() } };
	}

	@Test(dataProvider = "compressionData")
	public void testCompression(CompressionAlgorithm algorithm, SparkplugBPayload payload) throws Exception {

		// Compress the payload
		SparkplugBPayload compressedPayload = PayloadUtil.compress(payload, algorithm);

		// Test that there is a body (the compressed bytes)
		assertThat(compressedPayload.getBody() != null).isTrue();

		// Test that the sequence number is the same
		assertThat(compressedPayload.getSeq()).isEqualTo(payload.getSeq());

		// Test that the UUID is set correctly
		assertThat(compressedPayload.getUuid()).isEqualTo(PayloadUtil.UUID_COMPRESSED);

		// Decompress the payload
		SparkplugBPayload decompressedPayload = PayloadUtil.decompress(compressedPayload);

		// Test that the decompressed payload matches the original
		assertThat(decompressedPayload.getTimestamp()).isEqualTo(payload.getTimestamp());
		assertThat(decompressedPayload.getSeq()).isEqualTo(payload.getSeq());
		assertThat(decompressedPayload.getUuid()).isEqualTo(payload.getUuid());
		assertThat(Arrays.equals(decompressedPayload.getBody(), payload.getBody())).isTrue();
		// Test metrics
		List<Metric> decompressedMetrics = decompressedPayload.getMetrics();
		List<Metric> metrics = payload.getMetrics();
		for (int i = 0; i < metrics.size(); i++) {
			Metric decompressedMetric = decompressedMetrics.get(i);
			Metric metric = metrics.get(i);
			assertThat(decompressedMetric.getName()).isEqualTo(metric.getName());
			assertThat(decompressedMetric.getValue()).isEqualTo(metric.getValue());
			assertThat(decompressedMetric.getDataType()).isEqualTo(metric.getDataType());
		}

	}
}
