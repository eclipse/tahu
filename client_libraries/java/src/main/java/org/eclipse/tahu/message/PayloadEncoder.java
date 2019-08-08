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

import java.io.IOException;

/**
 * An interface for encoding payloads.
 * 
 * @param <P> the type of payload.
 */
public interface PayloadEncoder<P> {

	/**
	 * Converts a payload object into a byte array.
	 * 
	 * @param payload a payload object
	 * @return the byte array representing the payload
	 * @throws IOException
	 */
	public byte[] getBytes(P payload) throws IOException;
}
