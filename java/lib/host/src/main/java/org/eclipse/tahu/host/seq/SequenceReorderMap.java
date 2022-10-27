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

package org.eclipse.tahu.host.seq;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SequenceReorderMap {

	private static Logger logger = LoggerFactory.getLogger(SequenceReorderMap.class.getName());

	private final EdgeNodeDescriptor edgeNodeDescriptor;

	private final Map<Long, SequenceReorderContext> sequenceMap;

	private volatile long expectedSeqNum;

	private volatile Date lastUpdateTime;

	private final Object seqLock = new Object();

	public SequenceReorderMap(EdgeNodeDescriptor edgeNodeDescriptor) {
		this.edgeNodeDescriptor = edgeNodeDescriptor;
		expectedSeqNum = 0;
		lastUpdateTime = new Date();
		sequenceMap = new ConcurrentSkipListMap<>();
	}

	public EdgeNodeDescriptor getEdgeNodeDescriptor() {
		return edgeNodeDescriptor;
	}

	public long getNextExpectedSeqNum() {
		return expectedSeqNum;
	}

	public boolean liveSeqNumCheck(long toMatch) {
		synchronized (seqLock) {
			boolean match = (toMatch == expectedSeqNum);
			logger.trace("{} in liveSeqNumCheck - expected={} to actual={}", match ? "MATCHED" : "NOT MATCHED",
					expectedSeqNum, toMatch);
			if (match) {
				incrementExpectedSeqNum();
			}
			return match;
		}
	}

	/**
	 * Checks if a sequence number matches the current expected one. If true, it increments the expected sequence number
	 * and then returns the SequenceReorderConext for the message to be handled. It also removes that
	 * SequenceReorderConext from the Map
	 * 
	 * @param payload the {@link SparkplugBPayload} to handle
	 * @return the SequenceReorderContext associated with the payload if the sequence number check passed, otherwise
	 *         null is returned
	 */
	public SequenceReorderContext storedSeqNumCheck(long toMatch) {
		synchronized (seqLock) {
			SequenceReorderContext sequenceReorderContext = sequenceMap.remove(toMatch);
			if (sequenceReorderContext != null) {
				logger.trace("MATCHED in storedSeqNumCheck - Found stored message for {}", toMatch);
				incrementExpectedSeqNum();
			}
			return sequenceReorderContext;
		}
	}

	public void resetSeqNum() {
		synchronized (seqLock) {
			expectedSeqNum = 0;
		}
	}

	/**
	 * Increments the sequence number and wraps if required
	 */
	private void incrementExpectedSeqNum() {
		synchronized (seqLock) {
			// Update the last update time and increment
			lastUpdateTime = new Date();
			expectedSeqNum++;
			if (expectedSeqNum == 256) {
				expectedSeqNum = 0;
			}
		}
	}

	/**
	 * Adds a new {@link SequenceReorderContext} to the list by its sequence number
	 * 
	 * @param seqNum the sequence number key
	 * @param sequenceReorderContext the {@link SequenceReorderContext} associated with the sequence number
	 */
	public void put(long seqNum, SequenceReorderContext sequenceReorderContext) {
		synchronized (seqLock) {
			sequenceMap.put(seqNum, sequenceReorderContext);
		}
	}

	/**
	 * Removes all messages in the map that are older than the NBIRTH
	 *
	 * @param nBirthDate the {@link Date} associated with the incoming NBIRTH
	 */
	public void prune(Date nBirthDate) {
		if (nBirthDate == null) {
			logger.error("Attempting to prune messages from the SequenceReorderMap failed. NBIRTH timestamp is null");
			return;
		}

		synchronized (seqLock) {
			logger.debug("Pruning with date {}", nBirthDate);
			Iterator<SequenceReorderContext> it = sequenceMap.values().iterator();
			while (it.hasNext()) {
				SequenceReorderContext sequenceReorderContext = it.next();
				if (sequenceReorderContext != null && sequenceReorderContext.getPayload() != null
						&& sequenceReorderContext.getPayload().getTimestamp() != null
						&& sequenceReorderContext.getPayload().getTimestamp().before(nBirthDate)) {
					logger.debug("Removing old message {}", sequenceReorderContext.getTopic());
					it.remove();
				} else {
					logger.debug("Checked {} - not removing because {} is after {}", sequenceReorderContext.getTopic(),
							sequenceReorderContext.getPayload().getTimestamp(), nBirthDate);
				}
			}
		}
	}

	public void reset() {
		synchronized (seqLock) {
			expectedSeqNum = 0;
			lastUpdateTime = new Date();
			sequenceMap.clear();
		}
	}

	public SequenceReorderContext getExpiredSequenceReorderContext(long timeout) {
		synchronized (seqLock) {
			if (!sequenceMap.isEmpty()) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MILLISECOND, (int) (timeout * -1));
				if (lastUpdateTime.before(calendar.getTime())) {
					return sequenceMap.values().iterator().next();
				}
			}

			// Didn't find an expired entry
			return null;
		}
	}

	public int size() {
		synchronized (seqLock) {
			return sequenceMap.size();
		}
	}

	public boolean isEmpty() {
		synchronized (seqLock) {
			return sequenceMap.isEmpty();
		}
	}

	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}
}
