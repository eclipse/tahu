/********************************************************************************
 * Copyright (c) 2022 Cirrus Link Solutions and others
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

package org.eclipse.tahu.mqtt;

import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;

public class RandomStartupDelay {

	public static final String ERROR_MESSAGE =
			"Random Startup Delay must be of the form 'min-max' where min is the low end of the range and max is the high end of the range in milliseconds";

	private final String randomStartupDelayString;
	private final long low;
	private final long high;

	public RandomStartupDelay(String randomStartupDelayString) throws TahuException {
		if (randomStartupDelayString != null && !randomStartupDelayString.trim().isEmpty()) {
			String[] pair = randomStartupDelayString.split("-");
			if (pair.length == 2) {
				try {
					low = Long.parseLong(pair[0].trim());
					high = Long.parseLong(pair[1].trim());
					if (low < 0 || high < 0 || high < low) {
						throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, ERROR_MESSAGE);
					} else {
						this.randomStartupDelayString = randomStartupDelayString;
					}
				} catch (Exception e) {
					throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, ERROR_MESSAGE);
				}
			} else {
				throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, ERROR_MESSAGE);
			}
		} else {
			this.randomStartupDelayString = null;
			low = -1L;
			high = -1L;
		}
	}

	public String getRandomStartupDelayString() {
		return randomStartupDelayString;
	}

	public long getLow() {
		return low;
	}

	public long getHigh() {
		return high;
	}

	public boolean isValid() {
		return (low >= 0 && high >= low) ? true : false;
	}

	public long getRandomDelay() {
		if (randomStartupDelayString != null) {
			return low + (long) (Math.random() * (high - low));
		} else {
			return 0L;
		}
	}
}
