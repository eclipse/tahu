/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2023 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.mqtt.test;

import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.mqtt.MqttServerUrl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class MqttServerUrlTest {

	public MqttServerUrlTest() {
	}

	@DataProvider
	public Object[][] goodUrlData() throws Exception {
		return new Object[][] { { "tcp://localhost:1883", "tcp", "localhost", 1883 },
				{ "ssl://localhost:8883", "ssl", "localhost", 8883 },
				{ "tls://localhost:8883", "tls", "localhost", 8883 }, { "localhost:1883", "tcp", "localhost", 1883 },
				{ "tcp://localhost", "tcp", "localhost", 1883 }, { "localhost", "tcp", "localhost", 1883 }, };
	}

	@Test(
			dataProvider = "goodUrlData")
	public void testGoodMqttServerUrls(String url, String expectedProtol, String expectedFqdn, Integer expectedPort) {
		try {
			MqttServerUrl mqttServerUrl = new MqttServerUrl(url);
			Assert.assertEquals(mqttServerUrl.getProtocol(), expectedProtol);
			Assert.assertEquals(mqttServerUrl.getFqdn(), expectedFqdn);
			Assert.assertEquals(mqttServerUrl.getPort(), expectedPort);
		} catch (TahuException e) {
			Assert.fail();
		}
	}
}
