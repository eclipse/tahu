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

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.tahu.pi.dio.DioException;

import jdk.dio.gpio.GPIOPin;

/**
 * Defines Pibrella class
 */
public class Pibrella {

	private static Pibrella instance;

	// Pibrella I/O pins
	private Map<PibrellaPins, GPIOPin> pins;

	private Pibrella() {
		this.pins = new EnumMap<>(PibrellaPins.class);
	}

	/**
	 * Gets an instance of Pibrella class
	 * 
	 * @return an instance of Pibrella class as {@link Pibrella}
	 */
	public static Pibrella getInstance() {
		if (instance == null) {
			instance = new Pibrella();
		}
		return instance;
	}

	/**
	 * Gets registered Pibrella I/O pins
	 * 
	 * @return a map of registered Pibrella I/O pins as {@link Map<PibrellaPins, GPIOPin>}
	 */
	public Map<PibrellaPins, GPIOPin> getRegisteredPins() {
		return this.pins;
	}

	/**
	 * Registers specified Pibrella I/O pin
	 * 
	 * @param pin - Pibrella pin as {@link PibrellaPins}
	 * @param gpioPin - GPIO pin as {@link GPIOPin}
	 */
	public void registerPin(PibrellaPins pin, GPIOPin gpioPin) {
		this.pins.put(pin, gpioPin);
	}

	/**
	 * Gets specified Pibrella input
	 * 
	 * @param input - Pibrella input pin as {@link PibrellaInputPins}
	 * @return Pibrella Input pin as {@link PibrellaInputPin}
	 * @throws DioException
	 */
	public PibrellaInputPin getInput(PibrellaInputPins input) throws DioException {
		return PibrellaInputPin.getInstance(input);
	}

	/**
	 * Gets specified Pibrella output
	 * 
	 * @param output Pibrella output pin as {@link PibrellaOutputPins}
	 * @return Pibrella output pin as {@link PibrellaOutputPin}
	 * @throws DioException
	 */
	public PibrellaOutputPin getOutput(PibrellaOutputPins output) throws DioException {
		return PibrellaOutputPin.getInstance(output);
	}

	/**
	 * Gets Pibrella button
	 * 
	 * @return Pibrella button as {@link PibrellaButton}
	 * @throws DioException
	 */
	public PibrellaButton getButton() throws DioException {
		return PibrellaButton.getInstance();
	}

	/**
	 * Gets Pibrella buzzer
	 * 
	 * @return Pibrella buzzer as {@link PibrellaBuzzer}
	 * @throws DioException
	 */
	public PibrellaBuzzer getBuzzer() throws DioException {
		return PibrellaBuzzer.getInstance();
	}

	/**
	 * Gets specified Pibrella LED
	 * 
	 * @param led - Pibrella LED as {@link PibrellaLEDs}
	 * @return Pibrella LED as {@link PibrellaLED}
	 * @throws DioException
	 */
	public PibrellaLED getLED(PibrellaLEDs led) throws DioException {
		return PibrellaLED.getInstance(led);
	}

	/**
	 * Closes all Pibrella I/O pins
	 */
	public void closeAllIOpins() {
		this.pins.keySet().forEach(pin -> {
			GPIOPin gpioPin = this.pins.get(pin);
			try {
				gpioPin.close();
			} catch (IOException e) {
				System.out.println("Failed ot close Pibrella pin: " + pin.getDescription());
			}
		});
	}
}
