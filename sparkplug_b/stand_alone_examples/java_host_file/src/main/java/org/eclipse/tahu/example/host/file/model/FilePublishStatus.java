/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.example.host.file.model;

/**
 * Defines File Publish status
 */
public enum FilePublishStatus {

	NOT_SET(0, "Not Set"),

	// 1xx Informational
	CONTINUE(100, "Continue File Transfer"),
	IN_PROGRESS(101, "File Transfer In Progress"),
	TERMINATED(102, "File Transfer Terminated"),

	// 2xx Success
	SUCCESS(200, "Success"),

	// 4xx Transmission Side Error
	PUBLISH_FAILED(400, "File Transfer Failed"),
	SEQ_NUM_ERR_TRANSMISSION(401, "Sequence Number Error (Transmission)"),
	SPARKPLUG_PARSING_ERR(402, "Sparkplug Parsing Error"),
	RESOURCE_NOT_FOUND(404, "Resource Not Found"),
	TCLIENT_NOT_CONNECTED(405, "Transmission Client Is Not Connected"),
	REQUEST_TOUT(408, "Request Timeout"),

	// 5xx Engine Side Error
	SEQ_NUM_ERR_ENGINE(500, "Sequence Number Error (Engine)"),
	INVALID_METRICS(501, "Invalid Metrics"),

	MD5_ERR(502, "File MD5 Sum Error"),
	PARTIAL_MD5_ERR(503, "Message MD5 Sum Error"),
	FILE_WRITE_ERR(504, "Error Writing File"),
	RENAME_ERR(505, "Error Renaming Partial File");

	private int code;
	private String description;

	/**
	 * FilePublishStatus constructor
	 * 
	 * @param code - status code as {@link int}
	 * @param description - status code description as {@link String}
	 */
	private FilePublishStatus(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Reports status code
	 * 
	 * @return status code as {@link int}
	 */
	public int getCode() {
		return code;
	}

	/**
	 * Returns status description
	 * 
	 * @return status description as {@link String}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns an instance of the {@link FilePublishStatus} per supplied status code
	 * 
	 * @param statusCode - status code as {@link int}
	 * @return an instance of {@link filePublishStatus}
	 */
	public static FilePublishStatus getInstance(int statusCode) {
		FilePublishStatus ret = null;
		for (FilePublishStatus filePublishStatus : FilePublishStatus.values()) {
			if (filePublishStatus.getCode() == statusCode) {
				ret = filePublishStatus;
				break;
			}
		}
		if (ret == null) {
			ret = FilePublishStatus.NOT_SET;
		}
		return ret;
	}
}
