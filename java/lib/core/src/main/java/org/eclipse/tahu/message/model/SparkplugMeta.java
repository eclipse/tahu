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
