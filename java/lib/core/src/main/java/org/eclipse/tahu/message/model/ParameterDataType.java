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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration of data types for the value of a {@link Parameter} for a {@link Template}
 */
public class ParameterDataType {

	private static final Logger logger = LoggerFactory.getLogger(ParameterDataType.class.getName());

	// Basic Types
	public static final ParameterDataType Int8 = new ParameterDataType("Int8", 1, Byte.class);
	public static final ParameterDataType Int16 = new ParameterDataType("Int16", 2, Short.class);
	public static final ParameterDataType Int32 = new ParameterDataType("Int32", 3, Integer.class);
	public static final ParameterDataType Int64 = new ParameterDataType("Int64", 4, Long.class);
	public static final ParameterDataType UInt8 = new ParameterDataType("UInt8", 5, Short.class);
	public static final ParameterDataType UInt16 = new ParameterDataType("UInt16", 6, Integer.class);
	public static final ParameterDataType UInt32 = new ParameterDataType("UInt32", 7, Long.class);
	public static final ParameterDataType UInt64 = new ParameterDataType("UInt64", 8, BigInteger.class);
	public static final ParameterDataType Float = new ParameterDataType("Float", 9, Float.class);
	public static final ParameterDataType Double = new ParameterDataType("Double", 10, Double.class);
	public static final ParameterDataType Boolean = new ParameterDataType("Boolean", 11, Boolean.class);
	public static final ParameterDataType String = new ParameterDataType("String", 12, String.class);
	public static final ParameterDataType DateTime = new ParameterDataType("DateTime", 13, Date.class);
	public static final ParameterDataType Text = new ParameterDataType("Text", 14, String.class);

	// Unknown
	public static final ParameterDataType Unknown = new ParameterDataType("Unknown", 0, Object.class);

	protected static final Map<String, ParameterDataType> types = new HashMap<>();
	static {
		types.put("Int8", Int8);
		types.put("Int16", Int16);
		types.put("Int32", Int32);
		types.put("Int64", Int64);
		types.put("UInt8", UInt8);
		types.put("UInt16", UInt16);
		types.put("UInt32", UInt32);
		types.put("UInt64", UInt64);
		types.put("Float", Float);
		types.put("Double", Double);
		types.put("Boolean", Boolean);
		types.put("String", String);
		types.put("DateTime", DateTime);
		types.put("Text", Text);
		types.put("Unknown", Unknown);
	}

	@JsonInclude
	@JsonValue
	private final String type;

	@JsonIgnore
	private int intValue = 0;

	@JsonIgnore
	private Class<?> clazz = null;

	public ParameterDataType() {
		type = null;
	}

	public ParameterDataType(String type) {
		this.type = type;
		this.intValue = types.get(type).toIntValue();
		this.clazz = types.get(type).getClazz();
	}

	/**
	 * Constructor
	 *
	 * @param intValue the integer representation of this {@link ParameterDatatype}
	 *
	 * @param clazz the {@link Class} type of this {@link ParameterDataType}
	 */
	protected ParameterDataType(String type, int intValue, Class<?> clazz) {
		this.type = type;
		this.intValue = intValue;
		this.clazz = clazz;
	}

	/**
	 * Checks the type of this {@link ParameterDataType} against an {@link Object} value
	 *
	 * @param value the {@link Object} value to validate against the {@link ParameterDataType}
	 *
	 * @throws SparkplugInvalidTypeException if the validation of the {@link Object} value against the
	 *             {@link ParameterDataType} fails
	 */
	public void checkType(Object value) throws SparkplugInvalidTypeException {
		if (value != null && !clazz.isAssignableFrom(value.getClass())) {
			logger.warn("Failed type check - " + clazz + " != " + value.getClass().toString());
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
	 * Converts the integer representation of the data type into a {@link ParameterDataType} instance.
	 * 
	 * @param i the integer representation of the data type.
	 * @return a {@link ParameterDataType} instance.
	 */
	public static ParameterDataType fromInteger(int i) {
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
		builder.append("ParameterDataType [type=");
		builder.append(type);
		builder.append(", intValue=");
		builder.append(intValue);
		builder.append(", clazz=");
		builder.append(clazz);
		builder.append("]");
		return builder.toString();
	}
}
