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

/**
 * Defines digital output pin
 */
public class DigitalOutputPin extends DioPin {

	public DigitalOutputPin(String name) {
		super(name);
	}

	/**
	 * Sets pin high
	 * 
	 * @throws DioException
	 */
	public void setHigh() throws DioException {
		try {
			getGpioPin().setValue(true);
		} catch (Exception e) {
			throw new DioException("failed to set " + getPinName() + " HIGH", e);
		}
	}

	/**
	 * Sets pin low
	 * 
	 * @throws DioException
	 */
	public void setLow() throws DioException {
		try {
			getGpioPin().setValue(false);
		} catch (Exception e) {
			throw new DioException("failed to set " + getPinName() + " LOW", e);
		}
	}

	/**
	 * Sets pin state
	 * 
	 * @param state - pin state as {@link boolean}
	 * @throws DioException
	 */
	public void setState(boolean state) throws DioException {
		try {
			getGpioPin().setValue(state);
		} catch (Exception e) {
			throw new DioException("failed to set state of " + getPinName() + " to " + state, e);
		}
	}
}
