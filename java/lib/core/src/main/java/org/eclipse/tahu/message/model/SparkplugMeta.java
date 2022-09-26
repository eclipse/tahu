/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.model;

public class SparkplugMeta {

	public static final String SPARKPLUG_B_TOPIC_PREFIX = "spBv1.0";
	public static final String SPARKPLUG_TOPIC_HOST_STATE_TOKEN = "STATE";
	public static final String SPARKPLUG_TOPIC_HOST_STATE_PREFIX =
			SPARKPLUG_B_TOPIC_PREFIX + "/" + SPARKPLUG_TOPIC_HOST_STATE_TOKEN;
	public static final String SPARKPLUG_SEQUENCE_NUMBER_KEY = "seq";
	public static final String SPARKPLUG_BD_SEQUENCE_NUMBER_KEY = "bdSeq";

	// Properties
	public static final String QUALITY_PROP_NAME = "Quality";

	// Well Known Metrics
	public static final String METRIC_NODE_CONTROL = "Node Control";
	public static final String METRIC_NODE_REBIRTH = METRIC_NODE_CONTROL + "/" + "Rebirth";
}
