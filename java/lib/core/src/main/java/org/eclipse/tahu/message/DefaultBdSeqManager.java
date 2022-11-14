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

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultBdSeqManager implements BdSeqManager {

	private static Logger logger = LoggerFactory.getLogger(DefaultBdSeqManager.class.getName());

	private static final String SPARKPLUG_DIRNAME = "Tahu_Temp_Dir";

	private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final String BD_SEQ_NUM_FILE_NAME_PREFIX =
			TMP_DIR + (TMP_DIR.endsWith(FILE_SEPARATOR) ? "" : FILE_SEPARATOR) + SPARKPLUG_DIRNAME + FILE_SEPARATOR;

	private final String bdSeqNumFileName;

	public DefaultBdSeqManager(String fileName) {
		bdSeqNumFileName = BD_SEQ_NUM_FILE_NAME_PREFIX + fileName;
	}

	@Override
	public long getNextDeathBdSeqNum() {
		try {
			logger.info("bdSeqNumFileName: {}", bdSeqNumFileName);
			File bdSeqNumFile = new File(bdSeqNumFileName);
			if (bdSeqNumFile.exists()) {
				int bdSeqNum =
						Integer.parseInt(FileUtils.readFileToString(bdSeqNumFile, Charset.defaultCharset().toString()));
				logger.info("Next Death bdSeq number: {}", bdSeqNum);
				return bdSeqNum;
			} else {
				storeNextDeathBdSeqNum(0);
				return 0;
			}
		} catch (Exception e) {
			logger.error("Failed to get the bdSeq number from the persistent directory", e);
			storeNextDeathBdSeqNum(0);
			return 0;
		}
	}

	@Override
	public void storeNextDeathBdSeqNum(long bdSeqNum) {
		try {
			File bdSeqNumFile = new File(bdSeqNumFileName);
			FileUtils.writeStringToFile(bdSeqNumFile, Long.toString(bdSeqNum), Charset.defaultCharset().toString(),
					false);
		} catch (Exception e) {
			logger.error("Failed to write the bdSeq number to the persistent directory", e);
		}
	}
}
