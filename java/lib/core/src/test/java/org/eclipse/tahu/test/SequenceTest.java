/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.test;

import static org.assertj.core.api.Assertions.fail;

import java.util.Date;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.eclipse.tahu.message.PayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.testng.annotations.Test;

/**
 * Sparkplug Test class for encoding and decoding sparkplug payloads
 */
public class SequenceTest {

	public SequenceTest() {
	}

	@Test
	public void testEnDeCode() throws SparkplugInvalidTypeException {
		unit(null);
		unit(0L);
		unit(1L);
	}

	private void unit(Long seq) {
		Date currentTime = new Date(0L);

		// Encode
		SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
		PayloadDecoder<SparkplugBPayload> decoder = new SparkplugBPayloadDecoder();
		try {
			SparkplugBPayload initialPayload =
					new SparkplugBPayloadBuilder().setTimestamp(currentTime).setSeq(seq).createPayload();
			byte[] bytes = encoder.getBytes(initialPayload, false);
			SparkplugBPayload decodedPayload = decoder.buildFromByteArray(bytes, null);
			System.out.println("Initial: " + initialPayload);
			System.out.println(seq + ":       " + bytesToHex(bytes));
			System.out.println("Decoded: " + decodedPayload);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

	private static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 3];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j * 3] = hexArray[v >>> 4];
			hexChars[j * 3 + 1] = hexArray[v & 0x0F];
			hexChars[j * 3 + 2] = 0x20; // space separator
		}
		return new String(hexChars);
	}
}
