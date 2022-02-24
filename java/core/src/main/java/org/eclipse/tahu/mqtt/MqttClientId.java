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

import java.util.UUID;

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;

/**
 * Defines MQTT Client ID
 */
public class MqttClientId {

	private static final int MAX_CLIENT_ID_LENGTH = 23;

	/*
	 * MQTT client ID
	 */
	private String mqttClientId;

	/**
	 * MqttClientId constructor
	 * 
	 * @param mqttClientId - MQTT client ID as {@link String}
	 * @param checkClientIdLength - check length of MQTT Client ID? as {@link boolean}
	 * @throws TahuException
	 */
	public MqttClientId(String mqttClientId, boolean checkClientIdLength) throws TahuException {
		if (mqttClientId == null) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, "MQTT Client ID is not set");
		} else if (checkClientIdLength && mqttClientId.length() > 23) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT,
					"MQTT Client ID can not exceed " + MAX_CLIENT_ID_LENGTH + " characters in length");

		}
		this.mqttClientId = mqttClientId;
	}

	/**
	 * Generates MQTT client ID for supplied prefix string
	 * 
	 * @param clientIdPrefix - cloud ID prefix as {@link String}
	 * @return MQTT client ID as {@link String}
	 * @throws TahuException
	 */
	public static String generate(String clientIdPrefix) throws TahuException {
		if (clientIdPrefix != null && clientIdPrefix.length() > MAX_CLIENT_ID_LENGTH - 2) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT,
					"MQTT Client ID prefix can not exceed " + (MAX_CLIENT_ID_LENGTH - 2) + " characters in length");
		}
		return clientIdPrefix + "-" + UUID.randomUUID().toString().substring(0,
				MAX_CLIENT_ID_LENGTH - (clientIdPrefix != null ? clientIdPrefix.length() : 0) - 1);
	}

	/**
	 * Reports MQTT Client ID
	 * 
	 * @return MQTT Client ID as {@link String}
	 */
	public String getMqttClientId() {
		return mqttClientId;
	}

	@Override
	public String toString() {
		return mqttClientId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mqttClientId == null) ? 0 : mqttClientId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MqttClientId other = (MqttClientId) obj;
		if (mqttClientId == null) {
			if (other.mqttClientId != null) {
				return false;
			}
		} else if (!mqttClientId.equals(other.mqttClientId)) {
			return false;
		}
		return true;
	}
}
