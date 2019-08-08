/********************************************************************************
 * Copyright (c) 2017, 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.json.DeserializerModifier;
import org.eclipse.tahu.json.DeserializerModule;
import org.eclipse.tahu.message.SparkplugBPayloadDecoder;
import org.eclipse.tahu.message.SparkplugBPayloadEncoder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Utilities for Sparkplug Payload handling.
 */
public class PayloadUtil {

	private static Logger logger = LogManager.getLogger(PayloadUtil.class.getName());

	public static final String UUID_COMPRESSED = "SPBV1.0_COMPRESSED";

	public static final String METRIC_ALGORITHM = "algorithm";

	/**
	 * Serializes a {@link SparkplugBPayload} instance in to a JSON string.
	 * 
	 * @param payload a {@link SparkplugBPayload} instance
	 * @return a JSON string
	 * @throws JsonProcessingException
	 */
	public static String toJsonString(SparkplugBPayload payload) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(payload);
	}

	/**
	 * Deserializes a JSON string into a {@link SparkplugBPayload} instance.
	 * 
	 * @param payload a JSON string
	 * @return a {@link SparkplugBPayload} instance
	 * @throws JsonProcessingException
	 */
	public static SparkplugBPayload fromJsonString(String jsonString)
			throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new DeserializerModule(new DeserializerModifier()));
		return mapper.readValue(jsonString, SparkplugBPayload.class);
	}

	/**
	 * Returns a decompressed {@link SparkplugBPayload} instance from an existing payload. Will return the original
	 * payload if not compressed payload exists.
	 * 
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	public static SparkplugBPayload decompress(SparkplugBPayload payload) throws Exception {
		if (UUID_COMPRESSED.equals(payload.getUuid())) {
			logger.trace("Decompressing payload");
			SparkplugBPayloadDecoder decoder = new SparkplugBPayloadDecoder();
			CompressionConfig config = new CompressionConfig();
			byte[] decompressedBytes;
			List<Metric> metrics = payload.getMetrics();

			if (metrics != null && !metrics.isEmpty()) {
				for (Metric metric : metrics) {
					if (metric.getName().equals(METRIC_ALGORITHM)) {
						config.setAlgorithm(CompressionAlgorithm.valueOf(metric.getValue().toString()));
					}
				}
			}

			switch (config.getAlgorithm()) {
				case GZIP:
					decompressedBytes = GZipUtil.decompress(payload.getBody());
					break;
				case DEFLATE:
					decompressedBytes = inflateBytes(payload.getBody());
					break;
				default:
					throw new SparkplugException("Unknown or unsupported algorithm " + config.getAlgorithm());
			}

			// Decode bytes and return
			return decoder.buildFromByteArray(decompressedBytes);
		} else {
			logger.trace("Not decompressing payload");
			return payload;
		}
	}

	/**
	 * 
	 * @param payload
	 * @return
	 * @throws IOException
	 */
	public static SparkplugBPayload compress(SparkplugBPayload payload) throws IOException {
		logger.trace("Compressing payload");
		SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
		// Encode bytes
		byte[] encoded = encoder.getBytes(payload);

		// Default to DEFLATE
		byte[] compressedBytes = deflateBytes(encoded);

		// Create new payload, add the bytes as the body, and return.
		return new SparkplugBPayloadBuilder(payload.getSeq()).setBody(compressedBytes).setUuid(UUID_COMPRESSED)
				.createPayload();
	}

	/**
	 * 
	 * @param payload
	 * @return
	 * @throws IOException
	 */
	public static SparkplugBPayload compress(SparkplugBPayload payload, CompressionAlgorithm algorithm)
			throws IOException, SparkplugException {
		logger.trace("Compressing payload");
		SparkplugBPayloadEncoder encoder = new SparkplugBPayloadEncoder();
		// Encode bytes
		byte[] encoded = encoder.getBytes(payload);
		byte[] compressed = null;
		Metric algorithmMetric =
				new MetricBuilder(METRIC_ALGORITHM, MetricDataType.String, algorithm.toString()).createMetric();

		// Switch over compression algorithm
		switch (algorithm) {
			case GZIP:
				try {
					compressed = GZipUtil.compress(encoded);
				} catch (Exception e) {
					logger.error("Failed to GZIP the payload");
					throw new SparkplugException("Failed to GZIP the payload", e);
				}
				break;
			case DEFLATE:
				compressed = deflateBytes(encoded);
				break;
			default:
				throw new SparkplugException("Unknown or unsupported algorithm " + algorithm);
		}

		// Wrap and return the payload
		return new SparkplugBPayloadBuilder(payload.getSeq()).setBody(compressed).setUuid(UUID_COMPRESSED)
				.addMetric(algorithmMetric).createPayload();
	}

	/**
	 * Compresses a byte array using DEFLATE compression algorithm.
	 * 
	 * @param bytes the byte array to compress.
	 * @return the compressed byte array.
	 * @throws IOException
	 */
	protected static byte[] deflateBytes(byte[] bytes) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		Deflater deflater = new Deflater();
		deflater.setInput(bytes);
		deflater.finish();

		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			baos.write(buffer, 0, count);
		}
		baos.close();

		return baos.toByteArray();
	}

	/**
	 * Decompresses a byte array using DEFLATE compression algorithm.
	 * 
	 * @param bytes the byte array to decompress.
	 * @return the decompressed byte array.
	 * @throws IOException
	 * @throws DataFormatException
	 */
	protected static byte[] inflateBytes(byte[] bytes) throws IOException, DataFormatException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length);
		Inflater inflater = new Inflater();
		inflater.setInput(bytes);

		byte[] buffer = new byte[1024];
		while (!inflater.finished()) {
			int count = inflater.inflate(buffer);
			baos.write(buffer, 0, count);
		}
		baos.close();
		return baos.toByteArray();
	}

	private static class CompressionConfig {

		private CompressionAlgorithm algorithm;

		protected CompressionConfig() {
			this.algorithm = CompressionAlgorithm.DEFLATE;
		}

		protected CompressionAlgorithm getAlgorithm() {
			return algorithm;
		}

		protected void setAlgorithm(CompressionAlgorithm algorithm) {
			this.algorithm = algorithm;
		}
	}
}
