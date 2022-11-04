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

package org.eclipse.tahu.message.model;

public class SparkplugMeta {

	/**
	 * The root MQTT topic token for all Sparkplug B Messages
	 */
	public static final String SPARKPLUG_B_TOPIC_PREFIX = "spBv1.0";

	/**
	 * The Host Application MQTT topic token
	 */
	public static final String SPARKPLUG_TOPIC_HOST_STATE_TOKEN = "STATE";

	/**
	 * The full Host Application MQTT token prefix
	 */
	public static final String SPARKPLUG_TOPIC_HOST_STATE_PREFIX =
			SPARKPLUG_B_TOPIC_PREFIX + "/" + SPARKPLUG_TOPIC_HOST_STATE_TOKEN;

	/**
	 * The Sparkplug sequence number key for {@link Metric}s
	 */
	public static final String SPARKPLUG_SEQUENCE_NUMBER_KEY = "seq";

	/**
	 * The Sparkplug Birth/Death (BD) sequence number key used in Edge Node NBIRTH and NDEATH messages
	 */
	public static final String SPARKPLUG_BD_SEQUENCE_NUMBER_KEY = "bdSeq";

	/**
	 * The Sparkplug quality key
	 */
	public static final String QUALITY_PROP_NAME = "Quality";

	/**
	 * The Sparkplug 'Node Control' Metric prefix
	 */
	public static final String METRIC_NODE_CONTROL = "Node Control";

	/**
	 * The Sparkplug 'Node Control/Rebirth' Metric name
	 */
	public static final String METRIC_NODE_REBIRTH = METRIC_NODE_CONTROL + "/" + "Rebirth";
}
