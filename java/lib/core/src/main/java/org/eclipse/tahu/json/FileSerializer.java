/********************************************************************************
 * Copyright (c) 2017, 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu.json;

import java.io.IOException;

import org.eclipse.paho.client.mqttv3.internal.websocket.Base64;
import org.eclipse.tahu.message.model.File;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Serializes a {@link File} instance.
 */
public class FileSerializer extends StdSerializer<File> {

	/**
	 * Constructor.
	 */
	protected FileSerializer() {
		super(File.class);
	}

	/**
	 * Constructor.
	 * 
	 * @param clazz class.
	 */
	protected FileSerializer(Class<File> clazz) {
		super(clazz);
	}

	@Override
	public void serialize(File value, JsonGenerator generator, SerializerProvider provider) throws IOException {
		generator.writeString(Base64.encodeBytes(value.getBytes()));
	}
}
