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
package org.eclipse.tahu.pi.dio;

/**
 * Defines DIO Exception
 */
public class DioException extends Exception {

	private static final long serialVersionUID = 1L;

	public DioException(String msg) {
		super(msg);
	}

	public DioException(String message, Throwable cause) {
		super(message, cause);
	}
}
