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
import java.util.List;

import org.eclipse.tahu.SparkplugInvalidTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An enumeration of data types for values of a {@link PropertySet}
 */
public class PropertyDataType {

	// Basic Types
	public static final PropertyDataType Int8 = new PropertyDataType(1, Byte.class);
	public static final PropertyDataType Int16 = new PropertyDataType(2, Short.class);
	public static final PropertyDataType Int32 = new PropertyDataType(3, Integer.class);
	public static final PropertyDataType Int64 = new PropertyDataType(4, Long.class);
	public static final PropertyDataType UInt8 = new PropertyDataType(5, Short.class);
	public static final PropertyDataType UInt16 = new PropertyDataType(6, Integer.class);
	public static final PropertyDataType UInt32 = new PropertyDataType(7, Long.class);
	public static final PropertyDataType UInt64 = new PropertyDataType(8, BigInteger.class);
	public static final PropertyDataType Float = new PropertyDataType(9, Float.class);
	public static final PropertyDataType Double = new PropertyDataType(10, Double.class);
	public static final PropertyDataType Boolean = new PropertyDataType(11, Boolean.class);
	public static final PropertyDataType String = new PropertyDataType(12, String.class);
	public static final PropertyDataType DateTime = new PropertyDataType(13, Date.class);
	public static final PropertyDataType Text = new PropertyDataType(14, String.class);
	public static final PropertyDataType PropertySet = new PropertyDataType(20, PropertySet.class);
	public static final PropertyDataType PropertySetList = new PropertyDataType(21, List.class);
	public static final PropertyDataType Unknown = new PropertyDataType(0, Object.class);

	private static final Logger logger = LoggerFactory.getLogger(PropertyDataType.class.getName());

	private Class<?> clazz = null;
	private int intValue = 0;

	/**
	 * Constructor
	 *
	 * @param intValue the integer representation of this {@link PropertyDataType}
	 *
	 * @param clazz the {@link Class} type of this {@link PropertyDataType}
	 */
	private PropertyDataType(int intValue, Class<?> clazz) {
		this.intValue = intValue;
		this.clazz = clazz;
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

	/**
	 * Returns the class type for this DataType
	 * 
	 * @return the class type for this DataType
	 */
	public Class<?> getClazz() {
		return clazz;
	}
}
