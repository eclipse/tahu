/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2020 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.example.host.file.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines file utilities
 */
public class FileValidationUtils {

	private static Logger logger = LoggerFactory.getLogger(FileValidationUtils.class.getName());

	/**
	 * Default FileValidationUtils constructor
	 */
	private FileValidationUtils() {
		// no-op
	}

	/**
	 * Calculates MD5 sum of supplied byte array
	 * 
	 * @param bytes - data buffer as {@link byte[]}
	 * @return MD5 sum as {@link String}
	 */
	public static String calculateMd5Sum(byte[] bytes) {
		String hashString = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			hashString = DatatypeConverter.printHexBinary(md.digest(bytes));
		} catch (Exception e) {
			logger.error("Error checking MD5 sum", e);
		}
		return hashString != null ? hashString.toLowerCase() : null;
	}

	/**
	 * Calculates MD5 sum of supplied file
	 * 
	 * @param file - file name as {@link File}
	 * @return MD5 sum as {@link String}
	 */
	public static String calculateMd5Sum(String filename) {
		return calculateMd5Sum(new File(filename));
	}

	/**
	 * Calculates MD5 sum of supplied file
	 * 
	 * @param file - file object as {@link File}
	 * @return MD5 sum as {@link String}
	 */
	public static String calculateMd5Sum(File file) {
		String hashString = null;
		byte[] buffer = new byte[1024];
		try (InputStream fis = new FileInputStream(file)) {
			MessageDigest md = MessageDigest.getInstance("MD5");
			int numRead = 0;
			while (numRead != -1) {
				numRead = fis.read(buffer);
				if (numRead > 0) {
					md.update(buffer, 0, numRead);
				}
			}
			hashString = DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
		} catch (Exception e) {
			logger.error("Error checking MD5 sum of a file: {}", file, e);
		}
		return hashString != null ? hashString.toLowerCase() : null;
	}
}
