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

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;

public class MqttServerUrl {

	private final String mqttServerUrl;
	private final String protocol;
	private final String fqdn;
	private final Integer port;

	public MqttServerUrl(String mqttServerUrl) throws TahuException {
		this.mqttServerUrl = mqttServerUrl;

		try {
			String[] fqdnParts;
			if (mqttServerUrl.contains("://")) {
				String[] protocolParts = mqttServerUrl.split("://");
				protocol = protocolParts[0];
				fqdnParts = protocolParts[1].split(":");
			} else {
				protocol = "tcp";
				fqdnParts = mqttServerUrl.split(":");
			}

			if (fqdnParts.length == 1) {
				fqdn = fqdnParts[0];
				port = 1883;
			} else if (fqdnParts.length == 2) {
				fqdn = fqdnParts[0];
				port = Integer.parseInt(fqdnParts[1]);
			} else {
				throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, "Invalid MQTT Server URL: " + mqttServerUrl);
			}
		} catch (Exception e) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, "Invalid MQTT Server URL: " + mqttServerUrl, e);
		}
	}

	public MqttServerUrl(String protocol, String fqdn, Integer port) throws TahuException {
		if (protocol == null || fqdn == null || port == null) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT,
					"Invalid MQTT Server URL: protocol=" + protocol + " FQDN=" + fqdn + " port=" + port);
		} else {
			mqttServerUrl = protocol + "://" + fqdn + ":" + port;
			this.protocol = protocol;
			this.fqdn = fqdn;
			this.port = port;
		}
	}

	public static MqttServerUrl getMqttServerUrlSafe(String mqttServerUrl) {
		try {
			return new MqttServerUrl(mqttServerUrl);
		} catch (Exception e) {
			return null;
		}
	}

	public String getMqttServerUrl() {
		return mqttServerUrl;
	}

	public String getProtocol() {
		return protocol;
	}

	public String getFqdn() {
		return fqdn;
	}

	public Integer getPort() {
		return port;
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
