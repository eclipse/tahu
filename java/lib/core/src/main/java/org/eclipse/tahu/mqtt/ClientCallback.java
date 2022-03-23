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

package org.eclipse.tahu.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * A callback interface for usage with {@link TahuClient} instances.
 */
public interface ClientCallback {

	public void shutdown();

	public void messageArrived(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			String topic, MqttMessage message);

	public void connectionLost(MqttServerName mqttServerName, MqttServerUrl mqttServerUrl, MqttClientId clientId,
			Throwable cause);

	public void connectComplete(boolean reconnect, MqttServerName mqttServerName, MqttServerUrl mqttServerUrl,
			MqttClientId clientId);
}
