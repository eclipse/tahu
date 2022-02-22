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

package org.eclipse.tahu.util;

/**
 * An enumeration of supported payload compression algorithms
 */
public enum CompressionAlgorithm {

	GZIP,
	DEFLATE;

	public static CompressionAlgorithm parse(String algorithm) {
		return CompressionAlgorithm.valueOf(algorithm.toUpperCase());
	}
}
