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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Used to register the {@link DeserializerModifier} instance.
 */
public class DeserializerModule extends SimpleModule {

	private BeanDeserializerModifier deserializerModifier;

	public DeserializerModule(BeanDeserializerModifier deserializerModifier) {
		super("DeserializerModule", Version.unknownVersion());
		this.deserializerModifier = deserializerModifier;
	}

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);
		context.addBeanDeserializerModifier(deserializerModifier);
	}
}
