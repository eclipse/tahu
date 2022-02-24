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

public class MqttServerName {

	private String mqttServerName;

	public MqttServerName(String mqttServerName) {
		this.mqttServerName = mqttServerName;
	}

	public String getMqttServerName() {
		return mqttServerName;
	}

	@Override
	public String toString() {
		return mqttServerName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mqttServerName == null) ? 0 : mqttServerName.hashCode());
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
		MqttServerName other = (MqttServerName) obj;
		if (mqttServerName == null) {
			if (other.mqttServerName != null)
				return false;
		} else if (!mqttServerName.equals(other.mqttServerName))
			return false;
		return true;
	}
}
