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

import org.eclipse.tahu.pi.dio.DioException;
import org.eclipse.tahu.pi.dio.DioPin;

import jdk.dio.DeviceConfig;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 * Defines Pibrella input pin
 */
public class PibrellaInputPin extends DioPin {

	private static Map<PibrellaInputPins, PibrellaInputPin> inputs = new EnumMap<>(PibrellaInputPins.class);

	private PibrellaInputPin(PibrellaInputPins input) {
		super(input.getName());
	}

	/**
	 * Gets an instance of PibrellaInputPin class
	 * 
	 * @param input - pibrella input pin
	 * @return instance of PibrellaInputPin class as {@link PibrellaInputPin}
	 * @throws DioException
	 */
	public static PibrellaInputPin getInstance(PibrellaInputPins input) throws DioException {
		PibrellaInputPin pibrellaInput = inputs.get(input);
		if (inputs.get(input) == null) {
			pibrellaInput = new PibrellaInputPin(input);
			inputs.put(input, pibrellaInput);
		}
		GPIOPin gpioPin = Pibrella.getInstance().getRegisteredPins().get(input.getPin());
		if (gpioPin == null || !gpioPin.isOpen()) {
			gpioPin = open(input.getName(),
					new GPIOPinConfig(DeviceConfig.DEFAULT, input.getPin().getGPIO(), GPIOPinConfig.DIR_INPUT_ONLY,
							GPIOPinConfig.MODE_INPUT_PULL_DOWN, GPIOPinConfig.TRIGGER_BOTH_EDGES, false));
			pibrellaInput.setGpioPin(gpioPin);
			Pibrella.getInstance().registerPin(input.getPin(), gpioPin);
		}
		return pibrellaInput;
	}

	/**
	 * Closes all Pibrella input pins
	 */
	public static void closeAll() {
		inputs.values().forEach(input -> {
			try {
				input.close();
			} catch (Exception e) {
				System.out.println("failed to close " + input.getPinName());
			}
		});
	}
}
