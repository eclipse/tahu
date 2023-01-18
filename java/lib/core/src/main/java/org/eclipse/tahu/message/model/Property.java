/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.message.model;

public class Property<T> {

	private final String name;

	private final T defaultValue;

	private T value;

	public Property(String name, T defaultValue) {
		this.name = name;
		this.defaultValue = defaultValue;
	}

	public Property(String name, T defaultValue, T value) {
		this(name, defaultValue);
		this.value = value;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Property [name=");
		builder.append(name);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append(", value=");
		builder.append(value);
		builder.append("]");
		return builder.toString();
	}
}
