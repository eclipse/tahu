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

import java.util.EnumMap;
import java.util.Map;

import org.eclipse.tahu.pi.dio.DigitalOutputPin;
import org.eclipse.tahu.pi.dio.DioException;

import jdk.dio.DeviceConfig;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 * Defines Pibrella LED
 */
public class PibrellaLED extends DigitalOutputPin {

	private static Map<PibrellaLEDs, PibrellaLED> leds = new EnumMap<>(PibrellaLEDs.class);

	private PibrellaLED(PibrellaLEDs led) {
		super(led.getName());
	}

	/**
	 * Gets an instance of PibrellaLED class
	 * 
	 * @param led - Pibrellas LED
	 * @return instance of PibrellaLED class as {@link PibrellaLED}
	 * @throws DioException
	 */
	public static PibrellaLED getInstance(PibrellaLEDs led) throws DioException {
		PibrellaLED pibrellaLED = leds.get(led);
		if (leds.get(led) == null) {
			pibrellaLED = new PibrellaLED(led);
			leds.put(led, pibrellaLED);
		}
		GPIOPin gpioPin = Pibrella.getInstance().getRegisteredPins().get(led.getPin());
		if (gpioPin == null || !gpioPin.isOpen()) {
			gpioPin = open(led.getName(),
					new GPIOPinConfig(DeviceConfig.DEFAULT, led.getPin().getGPIO(), GPIOPinConfig.DIR_OUTPUT_ONLY,
							GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
			pibrellaLED.setGpioPin(gpioPin);
			Pibrella.getInstance().registerPin(led.getPin(), gpioPin);
		}
		return pibrellaLED;
	}

	/**
	 * Closes all LEDs
	 */
	public static void closeAll() {
		leds.values().forEach(led -> {
			try {
				led.close();
			} catch (Exception e) {
				System.out.println("failed to close " + led.getPinName());
			}
		});
	}

	/**
	 * Turns LED on
	 * 
	 * @throws DioException
	 */
	public void turnOn() throws DioException {
		setHigh();
	}

	/**
	 * Turns LED off
	 * 
	 * @throws DioException
	 */
	public void turnOff() throws DioException {
		setLow();
	}

	/**
	 * Reports if LED is on
	 * 
	 * @return LED state as {@link boolean}
	 * @throws DioException
	 */
	public boolean isOn() throws DioException {
		return isHigh();
	}
}
