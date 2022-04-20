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

import org.eclipse.tahu.pi.dio.DioException;
import org.eclipse.tahu.pi.dio.DioPin;

import jdk.dio.DeviceConfig;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 * Defines Pibrella button
 */
public class PibrellaButton extends DioPin {

	private static PibrellaButton instance;

	private PibrellaButton() {
		super(PibrellaPins.BUTTON.getName());
	}

	/**
	 * Gets an instance of Pibrella button
	 * 
	 * @return instance of Pibrella button as {@link PibrellaButton}
	 * @throws DioException
	 */
	public static PibrellaButton getInstance() throws DioException {
		if (instance == null) {
			instance = new PibrellaButton();
		}
		GPIOPin gpioPin = Pibrella.getInstance().getRegisteredPins().get(PibrellaPins.BUTTON);
		if (gpioPin == null || !gpioPin.isOpen()) {
			gpioPin = open(PibrellaPins.BUTTON.getName(),
					new GPIOPinConfig(DeviceConfig.DEFAULT, PibrellaPins.BUTTON.getGPIO(), GPIOPinConfig.DIR_INPUT_ONLY,
							GPIOPinConfig.MODE_INPUT_PULL_DOWN, GPIOPinConfig.TRIGGER_BOTH_EDGES, false));
			instance.setGpioPin(gpioPin);
			Pibrella.getInstance().registerPin(PibrellaPins.BUTTON, gpioPin);
		}
		return instance;
	}

	/**
	 * Reports if button is pressed
	 * 
	 * @return button state as {@link boolean}
	 * @throws DioException
	 */
	public boolean isPressed() throws DioException {
		return isHigh();
	}
}
