/********************************************************************************
 * Copyright (c) 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZipUtil {

	public static byte[] decompress(byte[] compressedData) throws Exception {
		ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
		GZIPInputStream gis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] decompressedData = null;
		int bufferSize = 1024;

		try {
			gis = new GZIPInputStream(bais);
			byte[] buffer = new byte[bufferSize];
			int bytesRead = -1;
			while ((bytesRead = gis.read(buffer, 0, bufferSize)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}

			baos.flush();
			decompressedData = baos.toByteArray();
		} finally {
			if (gis != null) {
				gis.close();
			}
			if (bais != null) {
				bais.close();
			}
			if (baos != null) {
				baos.close();
			}
		}
		return decompressedData;
	}

	public static byte[] compress(byte[] uncompressedData) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gos = null;
		byte[] compressedData = null;

		try {
			gos = new GZIPOutputStream(baos);
			gos.write(uncompressedData, 0, uncompressedData.length);
			gos.finish();
			gos.flush();
			baos.flush();
			compressedData = baos.toByteArray();
		} finally {
			if (gos != null) {
				gos.close();
			}
			if (baos != null) {
				baos.close();
			}
		}
		return compressedData;
	}
}
