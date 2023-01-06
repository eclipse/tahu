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

package org.eclipse.tahu.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.eclipse.tahu.message.model.Message;
import org.eclipse.tahu.message.model.Message.MessageBuilder;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MessageUtilTest {

	private Date testTime;

	public MessageUtilTest() {
		this.testTime = new Date();
	}

	@DataProvider
	public Object[][] messageData() throws Exception {
		return new Object[][] { { new Topic("spBv1.0", "G1", "E1", "D1", MessageType.DCMD),
				new SparkplugBPayloadBuilder().setTimestamp(testTime)
						.addMetric(new MetricBuilder("T1", MetricDataType.Int32, 12).timestamp(testTime).createMetric())
						.createPayload(),
				"{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"deviceId\":\"D1\",\"type\":\"DCMD\"},\"payload\":{\"timestamp\":"
						+ testTime.getTime() + ",\"metrics\":[{\"name\":\"T1\",\"timestamp\":" + testTime.getTime()
						+ ",\"dataType\":\"Int32\",\"value\":12}]}}" },
				{ new Topic("spBv1.0", "G1", "E1", "D2", MessageType.DCMD),
						new SparkplugBPayloadBuilder().setTimestamp(testTime)
								.addMetric(new MetricBuilder("T2", MetricDataType.String, "String Value")
										.timestamp(testTime).createMetric())
								.createPayload(),
						"{\"topic\":{\"namespace\":\"spBv1.0\",\"edgeNodeDescriptor\":\"G1/E1\",\"groupId\":\"G1\",\"edgeNodeId\":\"E1\",\"deviceId\":\"D2\",\"type\":\"DCMD\"},\"payload\":{\"timestamp\":"
								+ testTime.getTime() + ",\"metrics\":[{\"name\":\"T2\",\"timestamp\":"
								+ testTime.getTime() + ",\"dataType\":\"String\",\"value\":\"String Value\"}]}}" } };
	}

	@Test(
			dataProvider = "messageData")
	public void testCompression(Topic topic, SparkplugBPayload payload, String expectedJson) throws Exception {

		Message message = new MessageBuilder(topic, payload).build();
		String jsonString = MessageUtil.toJsonString(message);
		assertThat(jsonString).isEqualTo(expectedJson);
	}
}
