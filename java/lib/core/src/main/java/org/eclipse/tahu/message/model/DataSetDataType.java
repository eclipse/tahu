/********************************************************************************
 * Copyright (c) 2014-2024 Cirrus Link Solutions and others
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A enumeration of data types of values in a {@link DataSet}
 */
public class DataSetDataType {

	// Basic Types
	public static final DataSetDataType Int8 = new DataSetDataType("Int8", 1, Byte.class);
	public static final DataSetDataType Int16 = new DataSetDataType("Int16", 2, Short.class);
	public static final DataSetDataType Int32 = new DataSetDataType("Int32", 3, Integer.class);
	public static final DataSetDataType Int64 = new DataSetDataType("Int64", 4, Long.class);
	public static final DataSetDataType UInt8 = new DataSetDataType("UInt8", 5, Short.class);
	public static final DataSetDataType UInt16 = new DataSetDataType("UInt16", 6, Integer.class);
	public static final DataSetDataType UInt32 = new DataSetDataType("UInt32", 7, Long.class);
	public static final DataSetDataType UInt64 = new DataSetDataType("UInt64", 8, BigInteger.class);
	public static final DataSetDataType Float = new DataSetDataType("Float", 9, Float.class);
	public static final DataSetDataType Double = new DataSetDataType("Double", 10, Double.class);
	public static final DataSetDataType Boolean = new DataSetDataType("Boolean", 11, Boolean.class);
	public static final DataSetDataType String = new DataSetDataType("String", 12, String.class);
	public static final DataSetDataType DateTime = new DataSetDataType("DateTime", 13, Date.class);
	public static final DataSetDataType Text = new DataSetDataType("Text", 14, String.class);

	// Unknown
	public static final DataSetDataType Unknown = new DataSetDataType("Unknown", 0, Object.class);

	protected static final Map<String, DataSetDataType> types = new HashMap<>();
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

	public DataSetDataType() {
		type = null;
	}

	public DataSetDataType(String type) {
		this.type = type;
	}

	/**
	 * Constructor
	 *
	 * @param intValue the integer value of this {@link DataSetDataType}
	 *
	 * @param clazz the {@link Class} type associated with this {@link DataSetDataType}
	 */
	protected DataSetDataType(String type, int intValue, Class<?> clazz) {
		this.type = type;
		this.intValue = intValue;
		this.clazz = clazz;
	}

	public static DataSetDataType valueOf(String type) {
		return types.get(type);
	}

	public void checkType(Object value) throws SparkplugInvalidTypeException {
		if (value != null && !clazz.isAssignableFrom(value.getClass())) {
			throw new SparkplugInvalidTypeException("Value of type " + clazz.toString() + " is not assignable from "
					+ value.getClass().toString() + " where value is " + value, value.getClass());
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
	 * Converts the integer representation of the data type into a {@link DataSetDataType} instance.
	 * 
	 * @param i the integer representation of the data type.
	 * @return a {@link DataSetDataType} instance.
	 */
	public static DataSetDataType fromInteger(int i) {
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
		builder.append("DataSetDataType [type=");
		builder.append(type);
		builder.append(", intValue=");
		builder.append(intValue);
		builder.append(", clazz=");
		builder.append(clazz);
		builder.append("]");
		return builder.toString();
	}
}
