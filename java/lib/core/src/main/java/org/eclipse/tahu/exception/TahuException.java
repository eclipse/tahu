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

public class TahuException extends Exception {

	private static final long serialVersionUID = 1L;

	private TahuErrorCode code;

	public TahuException() {
		super();
	}

	public TahuException(TahuErrorCode code) {
		super();
		this.code = code;
	}

	public TahuException(TahuErrorCode code, String message, Throwable e) {
		super("ErrorCode: " + code.toString() + " - Message: " + message, e);
		this.code = code;
	}

	public TahuException(TahuErrorCode code, Throwable e) {
		super(code.toString(), e);
		this.code = code;
	}

	public TahuException(TahuErrorCode code, String message) {
		super(message);
		this.code = code;
	}

	public String getDetails() {
		return getMessage();
	}

	public TahuErrorCode getTahuErrorCode() {
		return code;
	}
}
