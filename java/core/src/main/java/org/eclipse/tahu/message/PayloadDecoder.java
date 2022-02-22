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

package org.eclipse.tahu.message;

/**
 * An interface for decoding payloads.
 * 
 * @param <P> the type of payload.
 */
public interface PayloadDecoder<P> {

	/**
	 * Builds a payload from a supplied byte array.
	 * 
	 * @param bytes the bytes representing the payload
	 * @return a payload object built from the byte array
	 * @throws Exception
	 */
	public P buildFromByteArray(byte[] bytes) throws Exception;
}
