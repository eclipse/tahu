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
 * Defines Pibrella output pin
 */
public class PibrellaOutputPin extends DigitalOutputPin {

	private static Map<PibrellaOutputPins, PibrellaOutputPin> outputs = new EnumMap<>(PibrellaOutputPins.class);

	private PibrellaOutputPin(PibrellaOutputPins output) {
		super(output.getName());
	}

	/**
	 * Gets an instance of PibrellaOutputPin class
	 * 
	 * @param output - Pibrella output pin
	 * @return instance of PibrellaOutputPin class as {@link PibrellaOutputPin}
	 * @throws DioException
	 */
	public static PibrellaOutputPin getInstance(PibrellaOutputPins output) throws DioException {
		PibrellaOutputPin pibrellaOutput = outputs.get(output);
		if (outputs.get(output) == null) {
			pibrellaOutput = new PibrellaOutputPin(output);
			outputs.put(output, pibrellaOutput);
		}
		GPIOPin gpioPin = Pibrella.getInstance().getRegisteredPins().get(output.getPin());
		if (gpioPin == null || !gpioPin.isOpen()) {
			gpioPin = open(output.getName(),
					new GPIOPinConfig(DeviceConfig.DEFAULT, output.getPin().getGPIO(), GPIOPinConfig.DIR_OUTPUT_ONLY,
							GPIOPinConfig.MODE_OUTPUT_PUSH_PULL, GPIOPinConfig.TRIGGER_NONE, false));
			pibrellaOutput.setGpioPin(gpioPin);
			Pibrella.getInstance().registerPin(output.getPin(), gpioPin);
		}
		return pibrellaOutput;
	}

	/**
	 * Closes all Pibrella output pins
	 */
	public static void closeAll() {
		outputs.values().forEach(output -> {
			try {
				output.close();
			} catch (Exception e) {
				System.out.println("failed to close " + output.getPinName());
			}
		});
	}
}
