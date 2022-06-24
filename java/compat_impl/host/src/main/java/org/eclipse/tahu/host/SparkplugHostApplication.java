/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.host;

import org.eclipse.tahu.mqtt.TahuClient;

public class SparkplugHostApplication {

	private TahuClient tahuClient;

	public SparkplugHostApplication() {
		// TODO Auto-generated constructor stub
	}

	public void start() {

	}

	public void stop() {
		tahuClient = new TahuClient(null, null, null, null, null, false, 0, null, null, null, null, false, null, null, false);
	}
}
