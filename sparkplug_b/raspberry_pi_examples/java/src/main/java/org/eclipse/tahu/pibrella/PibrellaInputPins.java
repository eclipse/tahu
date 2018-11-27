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
package org.eclipse.tahu.pibrella;

/**
 * Enumerates Pibrella input pins
 */
public enum PibrellaInputPins {
	A(PibrellaPins.INA),
	B(PibrellaPins.INB),
	C(PibrellaPins.INC),
	D(PibrellaPins.IND);
	
	private PibrellaPins pin;
	
	private PibrellaInputPins(PibrellaPins pin) {
		this.pin = pin;
	}
	
	public PibrellaPins getPin() {
		return this.pin;
	}
	
	public int getGPIO() {
		return this.pin.getGPIO();
	}
	
	public String getName() {
		return this.getPin().getName();
	}
}
