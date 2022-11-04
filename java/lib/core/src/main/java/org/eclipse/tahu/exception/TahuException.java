/********************************************************************************
 * Copyright (c) 2022 Cirrus Link Solutions and others
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

package org.eclipse.tahu.exception;

/**
 * A Sparkplug Exception
 */
public class TahuException extends Exception {

	private static final long serialVersionUID = 1L;

	private TahuErrorCode code;

	/**
	 * Default constructor.
	 */
	public TahuException() {
		super();
	}

	/**
	 * Constructor
	 *
	 * @param code the {@link TahuErrorCode} to associate with the {@link TahuException}
	 */
	public TahuException(TahuErrorCode code) {
		super();
		this.code = code;
	}

	/**
	 * Constructor
	 *
	 * @param code the {@link TahuErrorCode} to associate with the {@link TahuException}
	 * @param message a message to associate with the {@link TahuException}
	 * @param e an {@link Exception} that caused this {@link TahuException}
	 */
	public TahuException(TahuErrorCode code, String message, Throwable e) {
		super("ErrorCode: " + code.toString() + " - Message: " + message, e);
		this.code = code;
	}

	/**
	 * Constructor
	 *
	 * @param code the {@link TahuErrorCode} to associate with the {@link TahuException}
	 * @param e an {@link Exception} that caused this {@link TahuException}
	 */
	public TahuException(TahuErrorCode code, Throwable e) {
		super(code.toString(), e);
		this.code = code;
	}

	/**
	 * Constructor
	 *
	 * @param code the {@link TahuErrorCode} to associate with the {@link TahuException}
	 * @param message a message to associate with the {@link TahuException}
	 */
	public TahuException(TahuErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	/**
	 * Gets the string based message associated with this {@link TahuException}
	 *
	 * @return
	 */
	public String getDetails() {
		return getMessage();
	}

	/**
	 * Gets the {@link TahuErrorCode} associated with this {@link TahuException}
	 *
	 * @return
	 */
	public TahuErrorCode getTahuErrorCode() {
		return code;
	}
}
