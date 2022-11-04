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

package org.eclipse.tahu.message;

/**
 * Manages bdSeq numbers to be used by a Sparkplug Edge Node application
 */
public interface BdSeqManager {

	/**
	 * Gets the next sequential bdSeq number to be published by a Sparkplug Edge Node. This number MUST be one greater
	 * than the previous value returned unless the previous number was 255. If the previous value returned by this
	 * method was 255 the next value MUST be zero.
	 *
	 * @return a long value between 0 and 255 (inclusive) that is always one greater than the previous number returned
	 *         by this method
	 */
	public long getNextDeathBdSeqNum();

	/**
	 * Stores the next bdSeq number. This MUST override any next bdSeq number the {@link BdSeqManager} may have
	 * currently stored.
	 *
	 * @param bdSeqNum the bdSeq number to store in the {@link BdSeqManager}
	 */
	public void storeNextDeathBdSeqNum(long bdSeqNum);
}
