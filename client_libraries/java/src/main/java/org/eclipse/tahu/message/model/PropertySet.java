/********************************************************************************
 * Copyright (c) 2014, 2018 Cirrus Link Solutions and others
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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.tahu.SparkplugInvalidTypeException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * A class that maintains a set of properties associated with a {@link Metric}.
 */
public class PropertySet implements Map<String, PropertyValue> {

	@JsonIgnore
	private Map<String, PropertyValue> map;

	public PropertySet() {
		this.map = new HashMap<>();
	}

	private PropertySet(Map<String, PropertyValue> propertyMap) {
		this.map = propertyMap;
	}

	@JsonIgnore
	public PropertyValue getPropertyValue(String name) {
		return this.map.get(name);
	}

	@JsonIgnore
	public void setProperty(String name, PropertyValue value) {
		this.map.put(name, value);
	}

	@JsonIgnore
	public void removeProperty(String name) {
		this.map.remove(name);
	}

	@JsonIgnore
	public void clear() {
		this.map.clear();
	}

	@JsonIgnore
	public Set<String> getNames() {
		return map.keySet();
	}

	@JsonIgnore
	public Collection<PropertyValue> getValues() {
		return map.values();
	}

	@JsonIgnore
	public Map<String, PropertyValue> getPropertyMap() {
		return map;
	}

	@Override
	public String toString() {
		return "PropertySet [propertyMap=" + map + "]";
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public PropertyValue get(Object key) {
		return map.get(key);
	}

	@Override
	public PropertyValue put(String key, PropertyValue value) {
		return map.put(key, value);
	}

	@Override
	public PropertyValue remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends PropertyValue> m) {
		map.putAll(m);
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<PropertyValue> values() {
		return map.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, PropertyValue>> entrySet() {
		return map.entrySet();
	}

	/**
	 * A builder for a PropertySet instance
	 */
	public static class PropertySetBuilder {

		private Map<String, PropertyValue> propertyMap;

		public PropertySetBuilder() {
			this.propertyMap = new HashMap<>();
		}

		public PropertySetBuilder(Map<String, PropertyValue> propertyMap) {
			this.propertyMap = propertyMap;
		}

		public PropertySetBuilder(PropertySet propertySet) throws SparkplugInvalidTypeException {
			this.propertyMap = new HashMap<>();
			for (String name : propertySet.getNames()) {
				PropertyValue value = propertySet.getPropertyValue(name);
				propertyMap.put(name, new PropertyValue(value.getType(), value.getValue()));
			}
		}

		public PropertySetBuilder addProperty(String name, PropertyValue value) {
			this.propertyMap.put(name, value);
			return this;
		}

		public PropertySetBuilder addProperties(Map<String, PropertyValue> properties) {
			this.propertyMap.putAll(properties);
			return this;
		}

		public PropertySet createPropertySet() {
			return new PropertySet(propertyMap);
		}
	}
}
