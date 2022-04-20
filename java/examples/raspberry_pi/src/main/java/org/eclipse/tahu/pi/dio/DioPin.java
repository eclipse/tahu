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
package org.eclipse.tahu.pi.dio;

import jdk.dio.DeviceManager;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 * Defines digital I/O pin
 */
public class DioPin {

	private String pinName;
	private GPIOPin gpioPin;

	public DioPin(String name) {
		this.pinName = name;
	}

	/**
	 * Reports pin name
	 * 
	 * @return pin name as {@link String}
	 */
	public String getPinName() {
		return this.pinName;
	}

	/**
	 * Gets GPIO pin
	 * 
	 * @return GPIO pin as {@link GPIOPin}
	 */
	public GPIOPin getGpioPin() {
		return this.gpioPin;
	}

	/**
	 * Sets GPIO pin
	 * 
	 * @param gpioPin - GPIO pin as {@link GPIOPin}
	 */
	public void setGpioPin(GPIOPin gpioPin) {
		this.gpioPin = gpioPin;
	}

	public static GPIOPin open(String pinName, GPIOPinConfig gpioPinConfig) throws DioException {
		GPIOPin pin;
		try {
			pin = DeviceManager.open(GPIOPin.class, gpioPinConfig);
		} catch (Exception e) {
			throw new DioException("failed to open GPIO pin: " + pinName, e);
		}
		return pin;
	}

	/**
	 * Closes GPIO pin
	 * 
	 * @throws DioException
	 */
	public void close() throws DioException {
		try {
			if (this.gpioPin.isOpen()) {
				this.gpioPin.close();
			}
		} catch (Exception e) {
			throw new DioException("failed to close " + this.pinName, e);
		}
	}

	/**
	 * Reports if GPIO pin state
	 * 
	 * @return GPIO pin state as {@link boolean}
	 * @throws DioException
	 */
	public boolean isHigh() throws DioException {
		boolean ret = false;
		try {
			ret = this.gpioPin.getValue();
		} catch (Exception e) {
			throw new DioException("Failed obtain a state of " + this.pinName, e);
		}
		return ret;
	}
}
