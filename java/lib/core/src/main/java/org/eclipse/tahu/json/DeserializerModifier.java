/********************************************************************************
 * Copyright (c) 2017-2023 Cirrus Link Solutions and others
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

import org.eclipse.tahu.message.model.Metric;
import org.eclipse.tahu.message.model.PropertySet;
import org.eclipse.tahu.message.model.Template;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

/**
 * A {@link BeanDeserializerModifier} for Sparkplug
 */
public class DeserializerModifier extends BeanDeserializerModifier {

	@Override
	public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
			JsonDeserializer<?> deserializer) {
		if (Metric.class.equals(beanDesc.getBeanClass())) {
			return new MetricDeserializer(deserializer);
		} else if (Template.class.equals(beanDesc.getBeanClass())) {
			return new TemplateDeserializer(deserializer);
		} else if (PropertySet.class.equals(beanDesc.getBeanClass())) {
			return new PropertySetDeserializer(deserializer);
		}
		return super.modifyDeserializer(config, beanDesc, deserializer);
	}
}