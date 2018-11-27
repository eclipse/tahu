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

import org.eclipse.tahu.pi.dio.DigitalOutputPin;
import org.eclipse.tahu.pi.dio.DioException;

import jdk.dio.DeviceConfig;
import jdk.dio.gpio.GPIOPin;
import jdk.dio.gpio.GPIOPinConfig;

/**
 * Defines Pibrella buzzer
 */
public class PibrellaBuzzer extends DigitalOutputPin {

	private static PibrellaBuzzer instance;
	private Object lock = new Object();

	private PibrellaBuzzer() {
		super(PibrellaPins.BUZZER.getName());
	}

	/**
	 * Gets an instance of Pibrella buzzer
	 * 
	 * @return instance of Pibrella buzzer as {@link PibrellaBuzzer}
	 * @throws DioException
	 */
	public static PibrellaBuzzer getInstance() throws DioException {
		if (instance == null) {
			instance = new PibrellaBuzzer();
		}
		GPIOPin gpioPin = Pibrella.getInstance().getRegisteredPins().get(PibrellaPins.BUZZER);
		if (gpioPin == null || !gpioPin.isOpen()) {
			gpioPin = open(PibrellaPins.BUZZER.getName(),
					new GPIOPinConfig(DeviceConfig.DEFAULT, PibrellaPins.BUZZER.getGPIO(),
							GPIOPinConfig.DIR_OUTPUT_ONLY, GPIOPinConfig.MODE_OUTPUT_PUSH_PULL,
							GPIOPinConfig.TRIGGER_NONE, false));
			instance.setGpioPin(gpioPin);
			Pibrella.getInstance().registerPin(PibrellaPins.BUZZER, gpioPin);
		}
		return instance;
	}

	/**
	 * Starts the buzzer at specified frequency for a specified duration (in milliseconds)
	 *
	 * @param frequency as {@link int}
	 * @param duration number of milliseconds as {@link int}
	 */
	public void buzz(int frequency, int duration) {
		new Buzzer(frequency, duration).start();
	}

	/*
	 * Defines buzzer
	 */
	private class Buzzer extends Thread {

		private int frequency;
		private int duration;

		private Buzzer(int frequency, int duration) {
			this.frequency = frequency;
			this.duration = duration;
		}

		@Override
		public void run() {
			synchronized (lock) {
				long startTime = System.currentTimeMillis();
				int halfPeriod = 1000 / (2 * this.frequency);
				while ((System.currentTimeMillis() - startTime) < this.duration) {
					try {
						setHigh();
						lock.wait(halfPeriod);
						setLow();
					} catch (Exception e) {
						System.out.println("failed to buzz");
						break;
					}
				}
			}
		}
	}
}
