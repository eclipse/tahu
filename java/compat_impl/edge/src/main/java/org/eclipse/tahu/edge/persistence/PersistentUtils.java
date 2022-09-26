/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge.persistence;

import java.io.File;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistentUtils {

	private static Logger logger = LoggerFactory.getLogger(PersistentUtils.class.getName());

	private static final String SPARKPLUG_DIRNAME = "Tahu_Edge_Temp_Dir";

	private static final String TMP_DIR = System.getProperty("java.io.tmpdir");

	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private static final String BD_SEQ_NUM_FILE_NAME = TMP_DIR + SPARKPLUG_DIRNAME + FILE_SEPARATOR + "BD_SEQ_NUM";

	public static long getNextDeathBdSeqNum() {
		try {
			logger.info("BD_SEQ_NUM_FILE_NAME: {}", BD_SEQ_NUM_FILE_NAME);
			File bdSeqNumFile = new File(BD_SEQ_NUM_FILE_NAME);
			if (bdSeqNumFile.exists()) {
				long bdSeqNum = Long.parseLong(FileUtils.readFileToString(bdSeqNumFile, Charset.defaultCharset()));
				logger.info("Next Death bdSeq number: {}", bdSeqNum);
				return bdSeqNum;
			} else {
				return 0L;
			}
		} catch (Exception e) {
			logger.error("Failed to get the bdSeq number from the persistent directory", e);
			return 0L;
		}
	}

	public static void setNextDeathBdSeqNum(long bdSeqNum) {
		try {
			File bdSeqNumFile = new File(BD_SEQ_NUM_FILE_NAME);
			FileUtils.write(bdSeqNumFile, Long.toString(bdSeqNum), Charset.defaultCharset(), false);
		} catch (Exception e) {
			logger.error("Failed to write the bdSeq number to the persistent directory", e);
		}
	}
}
