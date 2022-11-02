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

package org.eclipse.tahu.edge.sim;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.tahu.SparkplugException;
import org.eclipse.tahu.message.model.DataSet;
import org.eclipse.tahu.message.model.DataSet.DataSetBuilder;
import org.eclipse.tahu.message.model.DataSetDataType;
import org.eclipse.tahu.message.model.DeviceDescriptor;
import org.eclipse.tahu.message.model.EdgeNodeDescriptor;
import org.eclipse.tahu.message.model.File;
import org.eclipse.tahu.message.model.MetaData;
import org.eclipse.tahu.message.model.MetaData.MetaDataBuilder;
import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.Metric.MetricBuilder;
import org.eclipse.tahu.message.model.MetricDataType;
import org.eclipse.tahu.message.model.Parameter;
import org.eclipse.tahu.message.model.ParameterDataType;
import org.eclipse.tahu.message.model.Row.RowBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayload;
import org.eclipse.tahu.message.model.SparkplugBPayload.SparkplugBPayloadBuilder;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap;
import org.eclipse.tahu.message.model.SparkplugBPayloadMap.SparkplugBPayloadMapBuilder;
import org.eclipse.tahu.message.model.SparkplugDescriptor;
import org.eclipse.tahu.message.model.Template;
import org.eclipse.tahu.message.model.Template.TemplateBuilder;
import org.eclipse.tahu.message.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomDataSimulator implements DataSimulator {

	private static Logger logger = LoggerFactory.getLogger(RandomDataSimulator.class.getName());

	private final int numNodeMetrics;
	private final Map<SparkplugDescriptor, Integer> numDeviceMetrics;

	private final Random random = new Random();
	private final Map<SparkplugDescriptor, Map<String, Metric>> metricMaps = new HashMap<>();
	private final Map<SparkplugDescriptor, Long> lastUpdateMap = new HashMap<>();

	public RandomDataSimulator(int numNodeMetrics, Map<SparkplugDescriptor, Integer> numDeviceMetrics) {
		this.numNodeMetrics = numNodeMetrics;
		this.numDeviceMetrics = numDeviceMetrics;
	}

	// DataSimulator API
	@Override
	public SparkplugBPayloadMap getNodeBirthPayload(EdgeNodeDescriptor edgeNodeDescriptor) {
		try {
			Date now = new Date();
			Map<String, Metric> metricMap = new HashMap<>();

			SparkplugBPayloadMapBuilder payloadBuilder = new SparkplugBPayloadMapBuilder();
			payloadBuilder.setTimestamp(now);

			// Add the Template definitions
			payloadBuilder
					.addMetric(new MetricBuilder("simpleType", MetricDataType.Template, newSimpleTemplate(true, null))
							.createMetric());
			payloadBuilder.addMetrics(newComplexTemplateDefs());

			// Add some random metrics
			for (int i = 0; i < numNodeMetrics; i++) {
				Metric metric = getRandomMetric("NT", i, true);
				metricMap.put(metric.getName(), metric);
				payloadBuilder.addMetric(metric);
			}

			metricMaps.put(edgeNodeDescriptor, metricMap);
			lastUpdateMap.put(edgeNodeDescriptor, now.getTime());
			return payloadBuilder.createPayload();
		} catch (Exception e) {
			logger.error("Failed to get the NBIRTH", e);
			return null;
		}
	}

	// DataSimulator API
	@Override
	public SparkplugBPayload getNodeDataPayload(EdgeNodeDescriptor edgeNodeDescriptor) {
		try {
			Date now = new Date();
			Map<String, Metric> metricMap = new HashMap<>();

			SparkplugBPayloadBuilder payloadBuilder = new SparkplugBPayloadBuilder();
			payloadBuilder.setTimestamp(now);
			logger.info("Getting number of metrics for {}", edgeNodeDescriptor);
			for (int i = 0; i < numNodeMetrics; i++) {
				Metric metric = getRandomMetric("NT", i, true);
				if (metric != null) {
					metricMap.put(metric.getName(), metric);
					payloadBuilder.addMetric(metric);
				}
			}

			metricMaps.put(edgeNodeDescriptor, metricMap);
			lastUpdateMap.put(edgeNodeDescriptor, now.getTime());
			return payloadBuilder.createPayload();
		} catch (Exception e) {
			logger.error("Failed to get the NDATA", e);
			return null;
		}
	}

	// DataSimulator API
	@Override
	public SparkplugBPayload getDeviceBirthPayload(DeviceDescriptor deviceDescriptor) {
		try {
			Date now = new Date();
			Map<String, Metric> metricMap = new HashMap<>();

			SparkplugBPayloadBuilder payloadBuilder = new SparkplugBPayloadBuilder();
			payloadBuilder.setTimestamp(now);
			logger.info("Getting number of metrics for {}", deviceDescriptor);
			for (int i = 0; i < numDeviceMetrics.get(deviceDescriptor); i++) {
				Metric metric = getRandomMetric("DT", i, true);
				if (metric != null) {
					metricMap.put(metric.getName(), metric);
					payloadBuilder.addMetric(metric);
				}
			}

			metricMaps.put(deviceDescriptor, metricMap);
			lastUpdateMap.put(deviceDescriptor, now.getTime());
			return payloadBuilder.createPayload();
		} catch (Exception e) {
			logger.error("Failed to get the DBIRTH", e);
			return null;
		}
	}

	// DataSimulator API
	@Override
	public SparkplugBPayload getDeviceDataPayload(DeviceDescriptor deviceDescriptor) {
		try {
			Date now = new Date();
			Map<String, Metric> metricMap = new HashMap<>();

			SparkplugBPayloadBuilder payloadBuilder = new SparkplugBPayloadBuilder();
			payloadBuilder.setTimestamp(now);
			logger.info("Getting number of metrics for {}", deviceDescriptor);
			for (int i = 0; i < numDeviceMetrics.get(deviceDescriptor); i++) {
				Metric metric = getRandomMetric("DT", i, true);
				if (metric != null) {
					metricMap.put(metric.getName(), metric);
					payloadBuilder.addMetric(metric);
				}
			}

			metricMaps.put(deviceDescriptor, metricMap);
			lastUpdateMap.put(deviceDescriptor, now.getTime());
			return payloadBuilder.createPayload();
		} catch (Exception e) {
			logger.error("Failed to get the DDATA", e);
			return null;
		}
	}

	// DataSimulator API
	@Override
	public boolean hasMetric(SparkplugDescriptor sparkplugDescriptor, String metricName) {
		if (metricMaps.containsKey(sparkplugDescriptor)
				&& metricMaps.get(sparkplugDescriptor).get(metricName) != null) {
			return true;
		} else {
			return false;
		}
	}

	// DataSimulator API
	@Override
	public Metric handleMetricWrite(SparkplugDescriptor sparkplugDescriptor, Metric metric) {
		// No-op for this simulator - just return the metric as though the value was 'written'
		return null;
	}

	private Metric getRandomMetric(String namePrefix, int index, boolean isBirth) throws Exception {
		int remainder = index % 34;
		int dataType = remainder + 1;

		// These are not valid MetricDataTypes - return an standard Int32
		if (dataType == 20 || dataType == 21) {
			return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int32, getRandomInt32()).createMetric();
		}

		switch (dataType) {
			case 1:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int8, getRandomInt8()).createMetric();
			case 2:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int16, getRandomInt16())
						.createMetric();
			case 3:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int32, getRandomInt32())
						.createMetric();
			case 4:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int64, getRandomInt64())
						.createMetric();
			case 5:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt8, getRandomUInt8())
						.createMetric();
			case 6:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt16, getRandomUInt16())
						.createMetric();
			case 7:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt32, getRandomUInt32())
						.createMetric();
			case 8:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt64, getRandomUInt64())
						.createMetric();
			case 9:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Float, random.nextFloat())
						.createMetric();
			case 10:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Double, random.nextDouble())
						.createMetric();
			case 11:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Boolean, random.nextBoolean())
						.createMetric();
			case 12:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.String, getRandomString(8))
						.createMetric();
			case 13:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.DateTime, new Date(random.nextLong()))
						.createMetric();
			case 14:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Text, getRandomString(8))
						.createMetric();
			case 15:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UUID, UUID.randomUUID().toString())
						.createMetric();
			case 16:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.DataSet, newDataSet()).createMetric();
			case 17:
				byte[] byteArray = new byte[10];
				random.nextBytes(byteArray);
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Bytes, byteArray).createMetric();
			case 18:
				byte[] fileDataArray = new byte[10];
				random.nextBytes(fileDataArray);
				byte[] md5 = MessageDigest.getInstance("MD5").digest(fileDataArray);
				String hashString = DatatypeConverter.printHexBinary(md5);
				MetaData metaData = new MetaDataBuilder().fileName("Fake_File.bin").md5(hashString).fileType("bin")
						.createMetaData();
				File file = new File("Fake_File.bin", fileDataArray);
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.File, file).metaData(metaData)
						.createMetric();
			case 19:
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Template,
						newComplexTemplateInstance()).createMetric();
			case 22:
				Byte[] int8ArrayValue = new Byte[5];
				for (int i = 0; i < 5; i++) {
					int8ArrayValue[i] = getRandomInt8();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int8Array, int8ArrayValue)
						.createMetric();
			case 23:
				Short[] int16ArrayValue = new Short[5];
				for (int i = 0; i < 5; i++) {
					int16ArrayValue[i] = getRandomInt16();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int16Array, int16ArrayValue)
						.createMetric();
			case 24:
				Integer[] int32ArrayValue = new Integer[5];
				for (int i = 0; i < 5; i++) {
					int32ArrayValue[i] = getRandomInt32();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int32Array, int32ArrayValue)
						.createMetric();
			case 25:
				Long[] int64ArrayValue = new Long[5];
				for (int i = 0; i < 5; i++) {
					int64ArrayValue[i] = getRandomInt64();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.Int64Array, int64ArrayValue)
						.createMetric();
			case 26:
				Short[] uInt8ArrayValue = new Short[5];
				for (int i = 0; i < 5; i++) {
					uInt8ArrayValue[i] = getRandomUInt8();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt8Array, uInt8ArrayValue)
						.createMetric();
			case 27:
				Integer[] uInt16rrayValue = new Integer[5];
				for (int i = 0; i < 5; i++) {
					uInt16rrayValue[i] = getRandomUInt16();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt16Array, uInt16rrayValue)
						.createMetric();
			case 28:
				Long[] uInt32ArrayValue = new Long[5];
				for (int i = 0; i < 5; i++) {
					uInt32ArrayValue[i] = getRandomUInt32();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt32Array, uInt32ArrayValue)
						.createMetric();
			case 29:
				BigInteger[] uInt64ArrayValue = new BigInteger[5];
				for (int i = 0; i < 5; i++) {
					uInt64ArrayValue[i] = getRandomUInt64();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.UInt64Array, uInt64ArrayValue)
						.createMetric();
			case 30:
				Float[] floatArrayValue = new Float[5];
				for (int i = 0; i < 5; i++) {
					floatArrayValue[i] = random.nextFloat();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.FloatArray, floatArrayValue)
						.createMetric();
			case 31:
				Double[] doubleArrayValue = new Double[5];
				for (int i = 0; i < 5; i++) {
					doubleArrayValue[i] = random.nextDouble();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.DoubleArray, doubleArrayValue)
						.createMetric();
			case 32:
				Boolean[] booleanArrayValue = new Boolean[5];
				for (int i = 0; i < 5; i++) {
					booleanArrayValue[i] = random.nextBoolean();
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.BooleanArray, booleanArrayValue)
						.createMetric();
			case 33:
				String[] stringArrayValue = new String[5];
				for (int i = 0; i < 5; i++) {
					stringArrayValue[i] = getRandomString(8);
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.StringArray, stringArrayValue)
						.createMetric();
			case 34:
				Date[] dateTimeArrayValue = new Date[5];
				for (int i = 0; i < 5; i++) {
					dateTimeArrayValue[i] = new Date(getRandomInt64());
				}
				return new MetricBuilder(namePrefix + "-" + index, MetricDataType.DateTimeArray, dateTimeArrayValue)
						.createMetric();
			default:
				logger.error("Failed to get a metric for dataType {}", dataType);
				return null;
		}
	}

	private DataSet newDataSet() throws SparkplugException {
		return new DataSetBuilder(14).addColumnName("Int8s").addColumnName("Int16s").addColumnName("Int32s")
				.addColumnName("Int64s").addColumnName("UInt8s").addColumnName("UInt16s").addColumnName("UInt32s")
				.addColumnName("UInt64s").addColumnName("Floats").addColumnName("Doubles").addColumnName("Booleans")
				.addColumnName("Strings").addColumnName("Dates").addColumnName("Texts").addType(DataSetDataType.Int8)
				.addType(DataSetDataType.Int16).addType(DataSetDataType.Int32).addType(DataSetDataType.Int64)
				.addType(DataSetDataType.UInt8).addType(DataSetDataType.UInt16).addType(DataSetDataType.UInt32)
				.addType(DataSetDataType.UInt64).addType(DataSetDataType.Float).addType(DataSetDataType.Double)
				.addType(DataSetDataType.Boolean).addType(DataSetDataType.String).addType(DataSetDataType.DateTime)
				.addType(DataSetDataType.Text)
				.addRow(new RowBuilder().addValue(new Value<Byte>(DataSetDataType.Int8, getRandomInt8()))
						.addValue(new Value<Short>(DataSetDataType.Int16, getRandomInt16()))
						.addValue(new Value<Integer>(DataSetDataType.Int32, getRandomInt32()))
						.addValue(new Value<Long>(DataSetDataType.Int64, getRandomInt64()))
						.addValue(new Value<Short>(DataSetDataType.UInt8, getRandomUInt8()))
						.addValue(new Value<Integer>(DataSetDataType.UInt16, getRandomUInt16()))
						.addValue(new Value<Long>(DataSetDataType.UInt32, getRandomUInt32()))
						.addValue(new Value<BigInteger>(DataSetDataType.UInt64, getRandomUInt64()))
						.addValue(new Value<Float>(DataSetDataType.Float, random.nextFloat()))
						.addValue(new Value<Double>(DataSetDataType.Double, random.nextDouble()))
						.addValue(new Value<Boolean>(DataSetDataType.Boolean, random.nextBoolean()))
						.addValue(new Value<String>(DataSetDataType.String, UUID.randomUUID().toString()))
						.addValue(new Value<Date>(DataSetDataType.DateTime, new Date()))
						.addValue(new Value<String>(DataSetDataType.Text, UUID.randomUUID().toString())).createRow())
				.addRow(new RowBuilder().addValue(new Value<Byte>(DataSetDataType.Int8, getRandomInt8()))
						.addValue(new Value<Short>(DataSetDataType.Int16, getRandomInt16()))
						.addValue(new Value<Integer>(DataSetDataType.Int32, getRandomInt32()))
						.addValue(new Value<Long>(DataSetDataType.Int64, getRandomInt64()))
						.addValue(new Value<Short>(DataSetDataType.UInt8, getRandomUInt8()))
						.addValue(new Value<Integer>(DataSetDataType.UInt16, getRandomUInt16()))
						.addValue(new Value<Long>(DataSetDataType.UInt32, getRandomUInt32()))
						.addValue(new Value<BigInteger>(DataSetDataType.UInt64, getRandomUInt64()))
						.addValue(new Value<Float>(DataSetDataType.Float, random.nextFloat()))
						.addValue(new Value<Double>(DataSetDataType.Double, random.nextDouble()))
						.addValue(new Value<Boolean>(DataSetDataType.Boolean, random.nextBoolean()))
						.addValue(new Value<String>(DataSetDataType.String, UUID.randomUUID().toString()))
						.addValue(new Value<Date>(DataSetDataType.DateTime, new Date()))
						.addValue(new Value<String>(DataSetDataType.Text, UUID.randomUUID().toString())).createRow())
				.createDataSet();
	}

	private Template newSimpleTemplate(boolean isDef, String templatRef) throws SparkplugException {
		List<Metric> metrics = new ArrayList<Metric>();
		metrics.add(new MetricBuilder("MyInt8", MetricDataType.Int8, getRandomInt8()).createMetric());
		metrics.add(new MetricBuilder("MyInt16", MetricDataType.Int16, getRandomInt16()).createMetric());
		metrics.add(new MetricBuilder("MyInt32", MetricDataType.Int32, getRandomInt32()).createMetric());
		metrics.add(new MetricBuilder("MyInt64", MetricDataType.Int64, getRandomInt64()).createMetric());
		metrics.add(new MetricBuilder("MyUInt8", MetricDataType.UInt8, getRandomUInt8()).createMetric());
		metrics.add(new MetricBuilder("MyUInt16", MetricDataType.UInt16, getRandomUInt16()).createMetric());
		metrics.add(new MetricBuilder("MyUInt32", MetricDataType.UInt32, getRandomUInt32()).createMetric());
		metrics.add(new MetricBuilder("MyUInt64", MetricDataType.UInt64, getRandomUInt64()).createMetric());
		metrics.add(new MetricBuilder("MyFloat", MetricDataType.Float, random.nextFloat()).createMetric());
		metrics.add(new MetricBuilder("MyDouble", MetricDataType.Double, random.nextDouble()).createMetric());
		metrics.add(new MetricBuilder("MyBoolean", MetricDataType.Boolean, random.nextBoolean()).createMetric());
		metrics.add(new MetricBuilder("MyString", MetricDataType.String, getRandomString(10)).createMetric());
		metrics.add(new MetricBuilder("MyDateTime", MetricDataType.DateTime, new Date()).createMetric());
		metrics.add(new MetricBuilder("MyText", MetricDataType.Text, getRandomString(10)).createMetric());
		metrics.add(new MetricBuilder("MyUUID", MetricDataType.UUID, UUID.randomUUID().toString()).createMetric());

		return new TemplateBuilder().version("v1.0").templateRef(templatRef).definition(isDef)
				.addParameters(newParams()).addMetrics(metrics).createTemplate();
	}

	private List<Parameter> newParams() throws SparkplugException {
		Random random = new Random();
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(new Parameter("ParamInt32", ParameterDataType.Int32, random.nextInt()));
		params.add(new Parameter("ParamFloat", ParameterDataType.Float, random.nextFloat()));
		params.add(new Parameter("ParamDouble", ParameterDataType.Double, random.nextDouble()));
		params.add(new Parameter("ParamBoolean", ParameterDataType.Boolean, random.nextBoolean()));
		params.add(new Parameter("ParamString", ParameterDataType.String, UUID.randomUUID().toString()));
		return params;
	}

	private List<Metric> newComplexTemplateDefs() throws Exception {
		ArrayList<Metric> metrics = new ArrayList<Metric>();

		// Add a new template "subType" definition with two primitive members
		metrics.add(new MetricBuilder("subType", MetricDataType.Template,
				new TemplateBuilder().definition(true).addParameters(newParams())
						.addMetric(new MetricBuilder("StringMember", MetricDataType.String, "value").createMetric())
						.addMetric(new MetricBuilder("IntegerMember", MetricDataType.Int32, 0).createMetric())
						.createTemplate()).createMetric());
		// Add new template "complexType" definition that contains an instance of "subType" as a member
		metrics.add(new MetricBuilder("complexType", MetricDataType.Template, new TemplateBuilder().definition(true)
				.addParameters(newParams())
				.addMetric(new MetricBuilder("subType", MetricDataType.Template, new TemplateBuilder().definition(false)
						.templateRef("subType")
						.addMetric(new MetricBuilder("StringMember", MetricDataType.String, "value").createMetric())
						.addMetric(new MetricBuilder("IntegerMember", MetricDataType.Int32, 0).createMetric())
						.createTemplate()).createMetric())
				.createTemplate()).createMetric());

		return metrics;
	}

	private Template newComplexTemplateInstance() throws Exception {
		// Create and return the template
		return new TemplateBuilder().definition(false).templateRef("complexType").addParameters(newParams())
				.addMetric(new MetricBuilder("subType", MetricDataType.Template, new TemplateBuilder().definition(false)
						.templateRef("subType").addParameters(newParams())
						.addMetric(new MetricBuilder("StringMember", MetricDataType.String, "myValue").createMetric())
						.addMetric(new MetricBuilder("IntegerMember", MetricDataType.Int32, 1).createMetric())
						.createTemplate()).createMetric())
				.createTemplate();
	}

	private byte getRandomInt8() {
		byte[] bytes = new byte[1];
		random.nextBytes(bytes);
		return bytes[0];
	}

	private short getRandomInt16() {
		return (short) random.nextInt(1 << 16);
	}

	private int getRandomInt32() {
		return random.nextInt();
	}

	private long getRandomInt64() {
		return random.nextLong();
	}

	private short getRandomUInt8() {
		return (short) random.nextInt(Short.MAX_VALUE + 1);
	}

	private int getRandomUInt16() {
		return ThreadLocalRandom.current().nextInt(0, 64535);

	}

	private long getRandomUInt32() {
		return ThreadLocalRandom.current().nextLong(4294967296L);
	}

	private BigInteger getRandomUInt64() {
		BigInteger minSize = new BigInteger("0");
		BigInteger maxSize = new BigInteger("18446744073709551616");
		BigInteger randomResult = new BigInteger(64, random);
		while (randomResult.compareTo(minSize) <= 0 || randomResult.compareTo(maxSize) >= 0) {
			randomResult = new BigInteger(64, random);
		}
		return randomResult;
	}

	private String getRandomString(int length) {
		return RandomStringUtils.randomAlphanumeric(length).toUpperCase();
	}
}
