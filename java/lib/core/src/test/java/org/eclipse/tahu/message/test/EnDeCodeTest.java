/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2023 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.test;

import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import junit.framework.TestCase;

public class EnDeCodeTest extends TestCase {

	public EnDeCodeTest(String testName) {
		super(testName);
	}

	public void testSimple() {
		assertTrue(true);
	}

	public void testEncodeDecode() {
		try {
			SparkplugBPayload originalPayload = new SparkplugBPayloadBuilder()
					.addMetric(
							new MetricBuilder("String", MetricDataType.String, "日本人 中國的 ~=[]()%+{}@;").createMetric())
					.addMetric(new MetricBuilder("StringArray", MetricDataType.StringArray,
							new String[] { "日本人 中國的 ~=[]()%+{}@;" }).createMetric())
					.addMetric(new MetricBuilder("StringArray", MetricDataType.StringArray,
							new String[] { "日本人 中國的 ~=[]()%+{}@;", "漢字" }).createMetric())
					.addMetric(new MetricBuilder("StringArray", MetricDataType.StringArray,
							new String[] { "漢字", "日本人 中國的 ~=[]()%+{}@;" }).createMetric())
					.addMetric(new MetricBuilder("StringArray", MetricDataType.StringArray,
							new String[] { "ناطرريننهمم ع نااارررر", "漢字", "日本人 中國的 ~=[]()%+{}@;" }).createMetric())
					.createPayload();
			byte[] encoded = new SparkplugBPayloadEncoder().getBytes(originalPayload, false);

			SparkplugBPayload decodedPayload = new SparkplugBPayloadDecoder().buildFromByteArray(encoded, null);

			assertEquals(((String[]) originalPayload.getMetrics().get(1).getValue())[0],
					((String[]) decodedPayload.getMetrics().get(1).getValue())[0]);

			assertEquals(((String[]) originalPayload.getMetrics().get(2).getValue())[0],
					((String[]) decodedPayload.getMetrics().get(2).getValue())[0]);
			assertEquals(((String[]) originalPayload.getMetrics().get(2).getValue())[1],
					((String[]) decodedPayload.getMetrics().get(2).getValue())[1]);

			assertEquals(((String[]) originalPayload.getMetrics().get(3).getValue())[0],
					((String[]) decodedPayload.getMetrics().get(3).getValue())[0]);
			assertEquals(((String[]) originalPayload.getMetrics().get(3).getValue())[1],
					((String[]) decodedPayload.getMetrics().get(3).getValue())[1]);

			assertEquals(((String[]) originalPayload.getMetrics().get(4).getValue())[0],
					((String[]) decodedPayload.getMetrics().get(4).getValue())[0]);
			assertEquals(((String[]) originalPayload.getMetrics().get(4).getValue())[1],
					((String[]) decodedPayload.getMetrics().get(4).getValue())[1]);
			assertEquals(((String[]) originalPayload.getMetrics().get(4).getValue())[2],
					((String[]) decodedPayload.getMetrics().get(4).getValue())[2]);
		} catch (Exception e) {
			System.out.println(e);
			fail();
		}
	}
}
