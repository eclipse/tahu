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

package org.eclipse.tahu.host;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.eclipse.tahu.exception.TahuErrorCode;
import org.eclipse.tahu.exception.TahuException;
import org.eclipse.tahu.message.model.Message;
import org.eclipse.tahu.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(CommandListener.class.getName());

	private ScheduledExecutorService executor;

	private CommandPublisher commandPublisher;

	private File fileDirectory;

	private long scanRate;

	public CommandListener(CommandPublisher commandPublisher, String fileDirectoryPath, long scanRate) {
		this.commandPublisher = commandPublisher;
		this.fileDirectory = new File(fileDirectoryPath);
		this.scanRate = scanRate;
	}

	public void start() throws TahuException {
		if (!fileDirectory.exists()) {
			logger.info("Creating file command listener directory at {}", fileDirectory.getPath());
			fileDirectory.mkdirs();
		} else if (!fileDirectory.isDirectory()) {
			throw new TahuException(TahuErrorCode.INVALID_ARGUMENT, "The specified directory '{}' is not a directory");
		}

		executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleWithFixedDelay(this, 0, scanRate, TimeUnit.MILLISECONDS);
	}

	public void shutdown() {
		executor.shutdownNow();
		executor = null;
	}

	@Override
	public void run() {
		try {
			Set<String> fileNames = Stream.of(fileDirectory.listFiles()).filter(file -> !file.isDirectory())
					.map(File::getName).collect(Collectors.toSet());

			if (fileNames != null && !fileNames.isEmpty()) {
				for (String fileName : fileNames) {
					fileName = fileDirectory.getAbsolutePath() + FileSystems.getDefault().getSeparator() + fileName;
					logger.info("Found file: {}", fileName);
					File commandFile = new File(fileName);
					String jsonContents = FileUtils.readFileToString(commandFile, StandardCharsets.UTF_8);

					try {
						Message message = MessageUtil.fromJsonString(jsonContents, true);
						logger.info("Message: {}", message);

						if (message != null) {
							commandPublisher.publishCommand(message.getTopic(), message.getPayload());

							logger.info("Deleting the file {}", commandFile);
							commandFile.delete();
						}
					} catch (Exception e) {
						logger.error("Failed to handle input file {}", fileName, e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("File scanning in the Comamnd Worker failed", e);
		}
	}
}
