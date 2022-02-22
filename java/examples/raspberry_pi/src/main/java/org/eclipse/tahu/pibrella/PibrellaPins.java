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

import org.eclipse.tahu.pi.dio.PinDirection;
import org.eclipse.tahu.pi.dio.Pins;

/**
 * Enumerates Pibrella I/O pins
 */
public enum PibrellaPins {
	INA(Pins.P21, PinDirection.INPUT, "Input A", "Inputs/a"),
	INB(Pins.P26, PinDirection.INPUT, "Input B", "Inputs/b"),
	INC(Pins.P24, PinDirection.INPUT, "Input C", "Inputs/c"),
	IND(Pins.P19, PinDirection.INPUT, "Input D", "Inputs/d"),
	BUTTON(Pins.P23, PinDirection.INPUT, "Button", "button"),
	OUTE(Pins.P15, PinDirection.OUTPUT, "Output E", "Outputs/e"),
	OUTF(Pins.P16, PinDirection.OUTPUT, "Output F", "Outputs/f"),
	OUTG(Pins.P18, PinDirection.OUTPUT, "Output G", "Outputs/g"),
	OUTH(Pins.P22, PinDirection.OUTPUT, "Output H", "Outputs/h"),
	LEDG(Pins.P7, PinDirection.OUTPUT, "Green LED", "Outputs/LEDs/green"),
	LEDY(Pins.P11, PinDirection.OUTPUT, "Yellow LED", "Outputs/LEDs/yellow"),
	LEDR(Pins.P13, PinDirection.OUTPUT, "Red LED", "Outputs/LEDs/red"),
	BUZZER(Pins.P12, PinDirection.OUTPUT, "Buzzer", "buzzer");
	
	private Pins pin;
	private PinDirection direction;
	private String name;
	private String description;
	
	private PibrellaPins(Pins pin, PinDirection direction, String name, String description) {
		this.pin = pin;
		this.direction = direction;
		this.name = name;
		this.description = description;
	}
	
	public Pins getPin() {
		return this.pin;
	}
	
	public int getGPIO() {
		return this.pin.getGPIO();
	}
	
	public PinDirection getDirection() {
		return this.direction;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
}
