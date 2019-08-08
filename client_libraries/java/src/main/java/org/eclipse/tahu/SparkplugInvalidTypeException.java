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

package org.eclipse.tahu;

/**
 * An Exception caused by an invalid type.
 */
public class SparkplugInvalidTypeException extends SparkplugException {

	private Class<?> type;

	public SparkplugInvalidTypeException(Class<?> type) {
		super("Invalid type " + type);
		this.type = type;
	}
}
