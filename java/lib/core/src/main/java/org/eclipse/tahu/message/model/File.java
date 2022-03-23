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

import java.util.Arrays;

import org.eclipse.tahu.json.FileSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonIgnoreProperties(value = { "fileName" })
@JsonSerialize(using = FileSerializer.class)
public class File {

	private String fileName;
	private byte[] bytes;

	public File() {
		super();
	}

	public File(String fileName, byte[] bytes) {
		super();
		this.fileName = fileName == null
				? null
				: fileName.replace("/", System.getProperty("file.separator")).replace("\\",
						System.getProperty("file.separator"));
		this.bytes = Arrays.copyOf(bytes, bytes.length);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("File [fileName=");
		builder.append(fileName);
		builder.append(", bytes=");
		builder.append(Arrays.toString(bytes));
		builder.append("]");
		return builder.toString();
	}
}
