/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.example.host.file;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.example.host.file.model.EdgeNode;
import org.eclipse.tahu.example.host.file.model.FilePublishStatus;
import org.eclipse.tahu.example.host.file.util.FileValidationUtils;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.MetaData;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.PropertySet;
import org.eclipse.tahu.message.model.PropertyValue;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines file assembler
 */
public class FileAssembler {

	private static Logger logger = LoggerFactory.getLogger(FileAssembler.class.getName());

	// Configurable constants
	private static final String FOLDER_PATH = "/tmp/receiver/";
	private static final boolean REPLACE_EXISTING_FILE = true; // Use false for 'keep existing file'
	private static final int MAX_NUMBER_RETRIES = 3;

	// Non-configurable constants
	private static final String TAG_PROVIDER_PROP_NAME = "filePublishingTagProvider";
	private static final String TAG_FOLDER_PATH_PROP_NAME = "filePublishingTagFolderPath";
	private static final String LAST_SEQ_NUM_PUBLISHED = "Last Published Sequence Number";
	private static final String PUBLISH_FILE_STATUS_CODE = "Publish Operation Status Code";
	private static final String FILE_SEPARATOR = System.getProperty("file.separator");

	private final ExecutorService executor;
	private final MqttClient client;

	private final String name;
	private String filename;
	private final EdgeNode edgeNode;
	private String deviceId;
	private long lastSeqNumProcessed = -1L;
	private boolean multipart;
	private String expectedMd5;
	private long numberOfFileChunks;
	private int retryCnt;

	/**
	 * FileAssembler constructor
	 * 
	 * @param name - file assembler's name as {@link String}
	 * @param edgeNode - Edge Node as {@link EdgeNode}
	 */
	public FileAssembler(ExecutorService executor, MqttClient client, String name, EdgeNode edgeNode) {
		this.executor = executor;
		this.client = client;
		this.name = name;
		this.edgeNode = edgeNode;
	}

	/**
	 * FileAssembler constructor
	 * 
	 * @param name - file assembler's name as {@link String}
	 * @param edgeNode - Edge Node as {@link EdgeNode}
	 * @param deviceId - Device Id as {@link String}
	 */
	public FileAssembler(ExecutorService executor, MqttClient client, String name, EdgeNode edgeNode, String deviceId) {
		this(executor, client, name, edgeNode);
		this.deviceId = deviceId;
	}

	/*
	 * Process supplied metric
	 */
	public FilePublishStatus processMetric(Metric metric) {
		if (!isValidMetric(metric)) {
			return FilePublishStatus.INVALID_METRICS;
		}
		MetaData metaData = metric.getMetaData();
		if (metaData.getSeq() == 0 && metaData.isMultiPart()) {
			multipart = true;
		}
		if (multipart && !metaData.isMultiPart()) {
			return FilePublishStatus.INVALID_METRICS;
		}
		FilePublishStatus fileAssemblerStatus = null;
		if (multipart) {
			fileAssemblerStatus = processFileMetricMultipart(metric);
			if (metaData.getSeq() > 0) {
				publishAckCommand(metric, fileAssemblerStatus);
			}
			logger.trace("The FileAssemblerStatus is {}", fileAssemblerStatus);
			if (!(fileAssemblerStatus == FilePublishStatus.CONTINUE
					|| fileAssemblerStatus == FilePublishStatus.SUCCESS)) {
				String fullDstFilePath = formAbsoluteDstFilePath(formDstFolderPath(), true);
				logger.trace("Deleting partial {} file", fullDstFilePath);
				FileUtils.deleteQuietly(new File(fullDstFilePath));
			}
		} else {
			logger.debug("About to process non-multipart file metric", metaData.getFileName());
			fileAssemblerStatus = processFileMetric(metric);
			publishAckCommand(metric, fileAssemblerStatus);
		}
		return fileAssemblerStatus;
	}

	/*
	 * Reports file assembler name
	 */
	String getName() {
		return name;
	}

	/*
	 * Processes metric of non-multipart file transfer
	 */
	private FilePublishStatus processFileMetric(Metric metric) {
		MetaData metaData = metric.getMetaData();
		filename = formDstFilename(metaData.getFileName());
		expectedMd5 = metaData.getMd5();

		org.eclipse.tahu.message.model.File fileValue = (org.eclipse.tahu.message.model.File) metric.getValue();
		byte[] fileData = fileValue.getBytes();

		if (!isValidMd5Sum(fileData, metaData.getMd5())) {
			logger.debug("MD5_ERR on {}", filename);
			return FilePublishStatus.MD5_ERR;
		}
		if (!writeToDstFile(fileData, false, false)) {
			logger.debug("FILE_WRITE_ERR on {}", filename);
			return FilePublishStatus.FILE_WRITE_ERR;
		}

		logger.trace("SUCCESS in processing file metric for {}", filename);
		return FilePublishStatus.SUCCESS;
	}

	/*
	 * Process supplied multipart metrics
	 */
	private FilePublishStatus processFileMetricMultipart(Metric metric) {
		MetaData metaData = metric.getMetaData();

		if (metaData.getSeq() == 0) {
			filename = formDstFilename(metaData.getFileName());
			expectedMd5 = metaData.getMd5();
			numberOfFileChunks = metaData.getSize();
			lastSeqNumProcessed = 0;
			logger.debug("Processing multipart file metrics :: Sequence Nnumber: 0, Total number of file chunks: {}",
					numberOfFileChunks);
			return FilePublishStatus.CONTINUE;
		}

		logger.debug("Processing multipart file metrics :: Sequence Number: {}, Total number of file chunks: {}",
				metaData.getSeq(), numberOfFileChunks);

		if (!REPLACE_EXISTING_FILE) {
			int extInd = metaData.getFileName().lastIndexOf('.');
			String metaDataFilenameNoExt =
					extInd > 0 ? metaData.getFileName().substring(0, extInd) : metaData.getFileName();
			if (!filename.startsWith(metaDataFilenameNoExt)) {
				return FilePublishStatus.INVALID_METRICS;
			}
		} else {
			if (!filename.equals(metaData.getFileName())) {
				return FilePublishStatus.INVALID_METRICS;
			}
		}

		if (metaData.getSeq() != lastSeqNumProcessed + 1) {
			return FilePublishStatus.SEQ_NUM_ERR_ENGINE;
		} else if (metaData.getSeq() == lastSeqNumProcessed) {
			if (retryCnt < MAX_NUMBER_RETRIES) {
				retryCnt++;
				return FilePublishStatus.CONTINUE;
			} else {
				return FilePublishStatus.SEQ_NUM_ERR_ENGINE;
			}
		}

		org.eclipse.tahu.message.model.File fileValue = (org.eclipse.tahu.message.model.File) metric.getValue();
		byte[] fileData = fileValue.getBytes();
		if (!isValidMd5Sum(fileData, metaData.getMd5())) {
			return FilePublishStatus.PARTIAL_MD5_ERR;
		}
		if (!writeToDstFile(fileData, true, metaData.getSeq() > 1)) {
			return FilePublishStatus.FILE_WRITE_ERR;
		}
		FilePublishStatus filePublishStatus = null;
		if (metaData.getSeq() == numberOfFileChunks) {
			File partialDstFile = new File(formAbsoluteDstFilePath(true));
			String fullDstFilePath = formAbsoluteDstFilePath(false);
			if (isValidMd5Sum(partialDstFile, expectedMd5)) {
				File dstFile = new File(formAbsoluteDstFilePath(false));
				if (partialDstFile.renameTo(dstFile)) {
					filePublishStatus = FilePublishStatus.SUCCESS;
				} else {
					filePublishStatus = FilePublishStatus.RENAME_ERR;
				}
			} else {
				logger.error("MD5 sum error after reassembly of the {} file", fullDstFilePath);
				filePublishStatus = FilePublishStatus.MD5_ERR;
			}
		} else if (metaData.getSeq() < numberOfFileChunks) {
			filePublishStatus = FilePublishStatus.CONTINUE;
		} else {
			filePublishStatus = FilePublishStatus.SEQ_NUM_ERR_ENGINE;
		}
		lastSeqNumProcessed = metaData.getSeq();
		return filePublishStatus;
	}

	/*
	 * Publishes ACK command
	 */
	private boolean publishAckCommand(Metric metric, FilePublishStatus filePublishStatus) {

		long seqNo = metric.getMetaData().getSeq();
		boolean ret = false;
		String cmdTopic = deviceId != null
				? new Topic(SparkplugExample.NAMESPACE, edgeNode.getGroupName(), edgeNode.getEdgeNodeName(), deviceId,
						MessageType.DCMD).toString()
				: new Topic(SparkplugExample.NAMESPACE, edgeNode.getGroupName(), edgeNode.getEdgeNodeName(),
						MessageType.NCMD).toString();

		// form payload
		SparkplugBPayload cmdPayload = new SparkplugBPayloadBuilder().setTimestamp(new Date()).createPayload();

		try {
			PropertySet propertySet = new PropertySet();
			PropertySet fileMetricProperties = metric.getProperties();
			if (fileMetricProperties != null && fileMetricProperties.containsKey(TAG_PROVIDER_PROP_NAME)) {
				PropertyValue fileMetricProperty = fileMetricProperties.get(TAG_PROVIDER_PROP_NAME);
				propertySet.put(TAG_PROVIDER_PROP_NAME, fileMetricProperty);
			}
			if (fileMetricProperties != null && fileMetricProperties.containsKey(TAG_FOLDER_PATH_PROP_NAME)) {
				PropertyValue fileMetricProperty = fileMetricProperties.get(TAG_FOLDER_PATH_PROP_NAME);
				propertySet.put(TAG_FOLDER_PATH_PROP_NAME, fileMetricProperty);
			}

			Metric cmdMetricSeqNum =
					new MetricBuilder(LAST_SEQ_NUM_PUBLISHED, MetricDataType.Int64, seqNo).createMetric();
			cmdMetricSeqNum.setProperties(propertySet);

			Metric cmdMetricStatusCode =
					new MetricBuilder(PUBLISH_FILE_STATUS_CODE, MetricDataType.Int32, filePublishStatus.getCode())
							.createMetric();
			cmdMetricStatusCode.setProperties(propertySet);

			cmdPayload.addMetric(cmdMetricSeqNum);
			cmdPayload.addMetric(cmdMetricStatusCode);
			logger.debug("Publishing file ACK to {}", cmdTopic);
			executor.execute(new Publisher(client, cmdTopic, cmdPayload, 0, false));
		} catch (SparkplugInvalidTypeException e) {
			logger.error("Failed to publish ACK command", e);
		}
		return ret;
	}

	/*
	 * Writes supplied data to the destination file
	 */
	private boolean writeToDstFile(byte[] fileData, boolean isPartial, boolean append) {
		boolean ret = false;

		// Write the file to the file system
		String fullDstFilePath = formAbsoluteDstFilePath(formDstFolderPath(), isPartial);
		if (!append) {
			if (REPLACE_EXISTING_FILE) {
				FileUtils.deleteQuietly(new File(fullDstFilePath));
			}
		}
		try {
			FileUtils.writeByteArrayToFile(new File(fullDstFilePath), fileData, append);
			ret = true;
		} catch (IOException e) {
			logger.error("Error writing file , {}, to file system", fullDstFilePath, e);
		}
		return ret;
	}

	/*
	 * Forms a path to the destination folder 
	 */
	private String formDstFolderPath() {
		if (new File(FOLDER_PATH).mkdirs()) {
			logger.trace("Created parent directories: {}", FOLDER_PATH);
		}
		return FOLDER_PATH;
	}

	/*
	 * Forms destination filename from fileneme supplied by the file metrics based on the file storing policy
	 */
	private String formDstFilename(String metaDataFilename) {
		if (REPLACE_EXISTING_FILE) {
			return metaDataFilename;
		}
		String ret = null;
		int fileExtInd = metaDataFilename.lastIndexOf('.');
		String fileNameNoExt = fileExtInd > 0 ? metaDataFilename.substring(0, fileExtInd) : metaDataFilename;
		String fileExt = fileExtInd > 0 ? metaDataFilename.substring(fileExtInd + 1) : "";

		String folderPath = formDstFolderPath();
		int fileNumber = -1;

		for (File f : new File(folderPath).listFiles((dir, fname) -> {
			int ind = fname.lastIndexOf('.');
			String ext = ind > 0 ? fname.substring(ind + 1) : "";
			return fname.startsWith(fileNameNoExt) && fileExt.equals(ext);
		})) {
			int fn = getFileNumber(f.getName());
			if (fn > fileNumber) {
				fileNumber = fn;
			}
		}

		if (fileNumber > 0) {
			ret = String.format("%s (%d).%s", fileNameNoExt, fileNumber + 1, fileExt);
		} else if (fileNumber == 0) {
			ret = String.format("%s (%d).%s", fileNameNoExt, 1, fileExt);
		} else {
			ret = metaDataFilename;
		}
		return ret;
	}

	/*
	 * Reports a number (i.e. copy number) for supplied file
	 */
	private int getFileNumber(String fname) {
		int ret = 0;
		int ind1 = fname.lastIndexOf('(');
		int ind2 = fname.lastIndexOf(')');
		if (ind1 > 0 && ind2 > ind1) {
			ret = Integer.parseInt(fname.substring(ind1 + 1, ind2));
		}
		return ret;
	}

	/*
	 * Forms an absolute path to the destination file
	 */
	private String formAbsoluteDstFilePath(boolean isPartial) {
		return formAbsoluteDstFilePath(formDstFolderPath(), isPartial);
	}

	/*
	 * Forms an absolute path to the destination file for supplied folder path
	 */
	private String formAbsoluteDstFilePath(String folderPath, boolean isPartial) {
		return isPartial
				? new StringBuilder().append(folderPath).append(FILE_SEPARATOR).append(filename).append(".part")
						.toString()
				: new StringBuilder().append(folderPath).append(FILE_SEPARATOR).append(filename).toString();
	}

	/*
	 * Reports if MD5 sum of supplied buffer containing file data matches expected one 
	 */
	private boolean isValidMd5Sum(byte[] fileData, String expectedMd5Sum) {
		boolean ret = true;
		if (expectedMd5Sum != null && !expectedMd5Sum.equalsIgnoreCase(FileValidationUtils.calculateMd5Sum(fileData))) {
			logger.error("Invalid MD5 sum");
			ret = false;
		}
		return ret;
	}

	/*
	 * Reports if MD5 sum of supplied file matches expected one 
	 */
	private boolean isValidMd5Sum(File file, String expectedMd5Sum) {
		boolean ret = true;
		if (expectedMd5Sum != null) {
			String calculatedMd5Sum = FileValidationUtils.calculateMd5Sum(file);
			if (!expectedMd5Sum.equalsIgnoreCase(calculatedMd5Sum)) {
				logger.error("Invalid MD5 sum: filename={}. Calculated MD5 Sum: {}. Expected MD5 Sum: {}",
						file.getAbsolutePath(), calculatedMd5Sum, expectedMd5Sum);
				ret = false;
			} else {
				logger.debug("MD5 sum match :: Calculated MD5 Sum: {}. Expected MD5 Sum: {}", calculatedMd5Sum,
						expectedMd5Sum);
			}
		}
		return ret;
	}

	/*
	 * Checks if supplied file metric is valid
	 */
	private boolean isValidMetric(Metric metric) {
		if (metric == null) {
			logger.error("Invalid 'File' metric: null");
			return false;
		}
		MetaData metaData = metric.getMetaData();
		if (metaData == null || metaData.isMultiPart() == null || metaData.getFileName() == null
				|| metaData.getSeq() < 0 || metaData.getSize() < 0) {
			logger.error("Invalid 'File' metric: {}", metric);
			return false;
		}
		logger.trace("Valid file metric for {}", metaData.getFileName());
		return true;
	}
}
