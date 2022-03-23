/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
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

import org.eclipse.tahu.SparkplugParsingException;

/**
 * An enumeration of Sparkplug MQTT message types. The type provides an indication as to what the MQTT Payload of
 * message will contain.
 */
public enum MessageType {

	/**
	 * Birth certificate for MQTT Edge of Network (EoN) Nodes.
	 */
	NBIRTH,

	/**
	 * Death certificate for MQTT Edge of Network (EoN) Nodes.
	 */
	NDEATH,

	/**
	 * Birth certificate for MQTT Devices.
	 */
	DBIRTH,

	/**
	 * Death certificate for MQTT Devices.
	 */
	DDEATH,

	/**
	 * Edge of Network (EoN) Node data message.
	 */
	NDATA,

	/**
	 * Device data message.
	 */
	DDATA,

	/**
	 * Edge of Network (EoN) Node command message.
	 */
	NCMD,

	/**
	 * Device command message.
	 */
	DCMD,

	/**
	 * Critical application state message.
	 */
	STATE,

	/**
	 * Device record message.
	 */
	DRECORD,

	/**
	 * Edge of Network (EoN) Node record message.
	 */
	NRECORD;

	public static MessageType parseMessageType(String type) throws SparkplugParsingException {
		for (MessageType messageType : MessageType.values()) {
			if (messageType.name().equals(type)) {
				return messageType;
			}
		}
		throw new SparkplugParsingException("Invalid message type: " + type);
	}

	public boolean isDeath() {
		return this.equals(DDEATH) || this.equals(NDEATH);
	}

	public boolean isCommand() {
		return this.equals(DCMD) || this.equals(NCMD);
	}

	public boolean isData() {
		return this.equals(DDATA) || this.equals(NDATA);
	}

	public boolean isBirth() {
		return this.equals(DBIRTH) || this.equals(NBIRTH);
	}

	public boolean isRecord() {
		return this.equals(DRECORD) || this.equals(NRECORD);
	}
}
