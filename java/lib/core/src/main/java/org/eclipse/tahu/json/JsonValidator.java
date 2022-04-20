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

package org.eclipse.tahu.json;

/**
 * Validates JSON.
 */
public class JsonValidator {

	protected static final String JSON_SCHEMA_FILENAME = "payload.json";

	private static JsonValidator instance = null;

	/**
	 * Constructor.
	 */
	protected JsonValidator() {
	}

	/**
	 * Returns the {@link JsonValidator} instance.
	 * 
	 * @return the {@link JsonValidator} instance.
	 */
	public static JsonValidator getInstance() {
		if (instance == null) {
			instance = new JsonValidator();
		}
		return instance;
	}

	/**
	 * Returns loads and returns the {@link JsonSchema} instance associated with this validator.
	 * 
	 * @return the {@link JsonSchema} instance associated with this validator.
	 * @throws IOException
	 * @throws ProcessingException
	 */
//	protected JsonSchema getSchema() throws IOException, ProcessingException {
//		// Get file from resources folder
//		ClassLoader classLoader = getClass().getClassLoader();
//		File schemaFile = new File(classLoader.getResource(JSON_SCHEMA_FILENAME).getFile());
//		return JsonSchemaFactory.byDefault().getJsonSchema(JsonLoader.fromFile(schemaFile));
//	}

	/**
	 * Returns true if the supplied JSON text is valid, false otherwise.
	 * 
	 * @param jsonText a {@link String} representing JSON text.
	 * @return true if the supplied JSON text is valid, false otherwise.
	 * @throws ProcessingException
	 * @throws IOException
	 */
//	public boolean isJsonValid(String jsonText) throws ProcessingException, IOException {
//		return getSchema().validate(JsonLoader.fromString(jsonText)).isSuccess();
//	}
}
