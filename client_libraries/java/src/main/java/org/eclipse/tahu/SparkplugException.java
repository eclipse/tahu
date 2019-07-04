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
 * A Sparkplug Exception
 */
public class SparkplugException extends Exception {

	/**
	 * Default constructor.
	 */
	public SparkplugException() {
	}

	/**
	 * Constructor
	 * 
	 * @param message an error message
	 */
	public SparkplugException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message an error message
	 * @param exception an underlying exception
	 */
	public SparkplugException(String message, Throwable exception) {
		super(message, exception);
	}

}
