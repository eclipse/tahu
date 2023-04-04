/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2023 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.json;

import java.io.IOException;

import org.eclipse.tahu.message.model.PropertySet;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Defines PropertySet deserializer
 */
public class PropertySetDeserializer extends StdDeserializer<PropertySet> implements ResolvableDeserializer {

	private static final long serialVersionUID = 1L;

	private final JsonDeserializer<?> defaultDeserializer;

	/**
	 * Constructor.
	 */
	protected PropertySetDeserializer(JsonDeserializer<?> defaultDeserializer) {
		super(PropertySet.class);
		this.defaultDeserializer = defaultDeserializer;
	}

	@Override
	public void resolve(DeserializationContext ctxt) throws JsonMappingException {
		((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
	}

	@Override
	public PropertySet deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException, JacksonException {
		PropertySet propSet = (PropertySet) defaultDeserializer.deserialize(parser, ctxt);
		return propSet;
	}
}
