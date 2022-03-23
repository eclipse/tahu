/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge.sim;

import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugDescriptor;

public interface DataSimulator {

	public SparkplugBPayloadMap getNBirthPayload(SparkplugDescriptor sparkplugDescriptor);

	public SparkplugBPayload getDBirth(SparkplugDescriptor sparkplugDescriptor);
}
