/********************************************************************************
 * Copyright (c) 2014-2022 Cirrus Link Solutions and others
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

package org.eclipse.tahu.message.model;

import java.math.BigInteger;
import java.util.Date;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration of data types associated with the value of a {@link Metric}
 */
public class MetricDataType {

	private static final Logger logger = LoggerFactory.getLogger(MetricDataType.class.getName());

	// Basic Types
	public static final MetricDataType Int8 = new MetricDataType("Int8", 1, Byte.class);
	public static final MetricDataType Int16 = new MetricDataType("Int16", 2, Short.class);
	public static final MetricDataType Int32 = new MetricDataType("Int32", 3, Integer.class);
	public static final MetricDataType Int64 = new MetricDataType("Int64", 4, Long.class);
	public static final MetricDataType UInt8 = new MetricDataType("UInt8", 5, Short.class);
	public static final MetricDataType UInt16 = new MetricDataType("UInt16", 6, Integer.class);
	public static final MetricDataType UInt32 = new MetricDataType("UInt32", 7, Long.class);
	public static final MetricDataType UInt64 = new MetricDataType("UInt64", 8, BigInteger.class);
	public static final MetricDataType Float = new MetricDataType("Float", 9, Float.class);
	public static final MetricDataType Double = new MetricDataType("Double", 10, Double.class);
	public static final MetricDataType Boolean = new MetricDataType("Boolean", 11, Boolean.class);
	public static final MetricDataType String = new MetricDataType("String", 12, String.class);
	public static final MetricDataType DateTime = new MetricDataType("DateTime", 13, Date.class);
	public static final MetricDataType Text = new MetricDataType("Text", 14, String.class);

	// Custom Types for Metrics
	public static final MetricDataType UUID = new MetricDataType("UUID", 15, String.class);
	public static final MetricDataType DataSet = new MetricDataType("DataSet", 16, DataSet.class);
	public static final MetricDataType Bytes = new MetricDataType("Bytes", 17, byte[].class);
	public static final MetricDataType File = new MetricDataType("File", 18, File.class);
	public static final MetricDataType Template = new MetricDataType("Template", 19, Template.class);

	// PropertyValue Types (20 and 21) are NOT metric datatypes

	// Array Types
	public static final MetricDataType Int8Array = new MetricDataType("Int8Array", 22, Byte[].class);
	public static final MetricDataType Int16Array = new MetricDataType("Int16Array", 23, Short[].class);
	public static final MetricDataType Int32Array = new MetricDataType("Int32Array", 24, Integer[].class);
	public static final MetricDataType Int64Array = new MetricDataType("Int64Array", 25, Long[].class);
	public static final MetricDataType UInt8Array = new MetricDataType("IUInt8Arraynt8", 26, Short[].class);
	public static final MetricDataType UInt16Array = new MetricDataType("UInt16Array", 27, Integer[].class);
	public static final MetricDataType UInt32Array = new MetricDataType("UInt32Array", 28, Long[].class);
	public static final MetricDataType UInt64Array = new MetricDataType("UInt64Array", 29, BigInteger[].class);
	public static final MetricDataType FloatArray = new MetricDataType("FloatArray", 30, Float[].class);
	public static final MetricDataType DoubleArray = new MetricDataType("DoubleArray", 31, Double[].class);
	public static final MetricDataType BooleanArray = new MetricDataType("BooleanArray", 32, Boolean[].class);
	public static final MetricDataType StringArray = new MetricDataType("StringArray", 33, String[].class);
	public static final MetricDataType DateTimeArray = new MetricDataType("DateTimeArray", 34, Date[].class);

	// Unknown
	public static final MetricDataType Unknown = new MetricDataType("Unknown", 0, Object.class);

	@JsonInclude
	@JsonValue
	private final String type;

	@JsonIgnore
	private int intValue = 0;

	@JsonIgnore
	private Class<?> clazz = null;

	public MetricDataType() {
		type = null;
	}

	public MetricDataType(String type) {
		this.type = type;
	}

	/**
	 * Constructor
	 *
	 * @param intValue the integer value of this {@link MetricDataType}
	 *
	 * @param clazz the {@link Class} type associated with this {@link MetricDataType}
	 */
	protected MetricDataType(String type, int intValue, Class<?> clazz) {
		this.type = type;
		this.intValue = intValue;
		this.clazz = clazz;
	}

	/**
	 * Checks the type of a specified value against the specified {@link MetricDataType}
	 *
	 * @param value the {@link Object} value to check against the {@link MetricDataType}
	 *
	 * @throws SparkplugInvalidTypeException if the value is not a valid type for the given {@link MetricDataType}
	 */
	public void checkType(Object value) throws SparkplugInvalidTypeException {
		if (value != null && !clazz.isAssignableFrom(value.getClass())) {
			logger.warn(
					"Failed type check - " + clazz + " != " + ((value != null) ? value.getClass().toString() : "null"));
			throw new SparkplugInvalidTypeException(value.getClass());
		}
	}

	/**
	 * Returns an integer representation of the data type.
	 * 
	 * @return an integer representation of the data type.
	 */
	public int toIntValue() {
		return this.intValue;
	}

	/**
	 * Converts the integer representation of the data type into a {@link MetricDataType} instance.
	 * 
	 * @param i the integer representation of the data type.
	 * @return a {@link MetricDataType} instance.
	 */
	public static MetricDataType fromInteger(int i) {
		switch (i) {
			case 1:
				return Int8;
			case 2:
				return Int16;
			case 3:
				return Int32;
			case 4:
				return Int64;
			case 5:
				return UInt8;
			case 6:
				return UInt16;
			case 7:
				return UInt32;
			case 8:
				return UInt64;
			case 9:
				return Float;
			case 10:
				return Double;
			case 11:
				return Boolean;
			case 12:
				return String;
			case 13:
				return DateTime;
			case 14:
				return Text;
			case 15:
				return UUID;
			case 16:
				return DataSet;
			case 17:
				return Bytes;
			case 18:
				return File;
			case 19:
				return Template;
			case 22:
				return Int8Array;
			case 23:
				return Int16Array;
			case 24:
				return Int32Array;
			case 25:
				return Int64Array;
			case 26:
				return UInt8Array;
			case 27:
				return UInt16Array;
			case 28:
				return UInt32Array;
			case 29:
				return UInt64Array;
			case 30:
				return FloatArray;
			case 31:
				return DoubleArray;
			case 32:
				return BooleanArray;
			case 33:
				return StringArray;
			case 34:
				return DateTimeArray;
			default:
				return Unknown;
		}
	}

	public String getType() {
		return type;
	}

	/**
	 * Returns the class type for this DataType
	 * 
	 * @return the class type for this DataType
	 */
	public Class<?> getClazz() {
		return clazz;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetricDataType [type=");
		builder.append(type);
		builder.append(", intValue=");
		builder.append(intValue);
		builder.append(", clazz=");
		builder.append(clazz);
		builder.append("]");
		return builder.toString();
	}
}
