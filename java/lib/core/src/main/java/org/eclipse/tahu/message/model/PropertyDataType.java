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
import java.util.List;
import java.util.Map;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * An enumeration of data types for values of a {@link PropertySet}
 */
public class PropertyDataType {

	private static final Logger logger = LoggerFactory.getLogger(PropertyDataType.class.getName());

	// Basic Types
	public static final PropertyDataType Int8 = new PropertyDataType("Int8", 1, Byte.class);
	public static final PropertyDataType Int16 = new PropertyDataType("Int16", 2, Short.class);
	public static final PropertyDataType Int32 = new PropertyDataType("Int32", 3, Integer.class);
	public static final PropertyDataType Int64 = new PropertyDataType("Int64", 4, Long.class);
	public static final PropertyDataType UInt8 = new PropertyDataType("UInt8", 5, Short.class);
	public static final PropertyDataType UInt16 = new PropertyDataType("UInt16", 6, Integer.class);
	public static final PropertyDataType UInt32 = new PropertyDataType("UInt32", 7, Long.class);
	public static final PropertyDataType UInt64 = new PropertyDataType("UInt64", 8, BigInteger.class);
	public static final PropertyDataType Float = new PropertyDataType("Float", 9, Float.class);
	public static final PropertyDataType Double = new PropertyDataType("Double", 10, Double.class);
	public static final PropertyDataType Boolean = new PropertyDataType("Boolean", 11, Boolean.class);
	public static final PropertyDataType String = new PropertyDataType("String", 12, String.class);
	public static final PropertyDataType DateTime = new PropertyDataType("DateTime", 13, Date.class);
	public static final PropertyDataType Text = new PropertyDataType("Text", 14, String.class);
	public static final PropertyDataType PropertySet = new PropertyDataType("PropertySet", 20, PropertySet.class);
	public static final PropertyDataType PropertySetList = new PropertyDataType("PropertySetList", 21, List.class);
	public static final PropertyDataType Unknown = new PropertyDataType("Unknown", 0, Object.class);

	protected static final Map<String, PropertyDataType> types = new HashMap<>();
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
		types.put("PropertySet", PropertySet);
		types.put("PropertySetList", PropertySetList);
		types.put("Unknown", Unknown);
	}

	@JsonInclude
	@JsonValue
	private final String type;

	@JsonIgnore
	private int intValue = 0;

	@JsonIgnore
	private Class<?> clazz = null;

	public PropertyDataType() {
		type = null;
	}

	public PropertyDataType(String type) {
		this.type = type;
	}

	/**
	 * Constructor
	 *
	 * @param intValue the integer representation of this {@link PropertyDataType}
	 *
	 * @param clazz the {@link Class} type of this {@link PropertyDataType}
	 */
	protected PropertyDataType(String type, int intValue, Class<?> clazz) {
		this.type = type;
		this.intValue = intValue;
		this.clazz = clazz;
	}

	public static PropertyDataType valueOf(String type) {
		return types.get(type);
	}

	/**
	 * Checks the type of this {@link PropertyDataType} against an {@link Object} value
	 *
	 * @param value the {@link Object} value to validate against the {@link PropertyDataType}
	 *
	 * @throws SparkplugInvalidTypeException if the validation of the {@link Object} value against the
	 *             {@link PropertyDataType} fails
	 */
	public void checkType(Object value) throws SparkplugInvalidTypeException {
		if (value != null && !clazz.isAssignableFrom(value.getClass())) {
			if (clazz == List.class && value instanceof List) {
				// Allow List subclasses
			} else {
				logger.warn("Failed type check - " + clazz + " != " + value.getClass().toString());
				throw new SparkplugInvalidTypeException(value.getClass());
			}
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
	 * Converts the integer representation of the data type into a {@link PropertyDataType} instance.
	 * 
	 * @param i the integer representation of the data type.
	 * @return a {@link PropertyDataType} instance.
	 */
	public static PropertyDataType fromInteger(int i) {
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
			case 20:
				return PropertySet;
			case 21:
				return PropertySetList;
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
		builder.append("PropertyDataType [type=");
		builder.append(type);
		builder.append(", intValue=");
		builder.append(intValue);
		builder.append(", clazz=");
		builder.append(clazz);
		builder.append("]");
		return builder.toString();
	}
}
