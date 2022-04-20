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

public class MqttServerUrl {

	private String mqttServerUrl;

	public MqttServerUrl(String mqttServerUrl) {
		this.mqttServerUrl = mqttServerUrl;
	}

	public String getMqttServerUrl() {
		return mqttServerUrl;
	}

	@Override
	public String toString() {
		return mqttServerUrl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mqttServerUrl == null) ? 0 : mqttServerUrl.hashCode());
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
		MqttServerUrl other = (MqttServerUrl) obj;
		if (mqttServerUrl == null) {
			if (other.mqttServerUrl != null)
				return false;
		} else if (!mqttServerUrl.equals(other.mqttServerUrl))
			return false;
		return true;
	}
}
