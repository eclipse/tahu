/********************************************************************************
 * Copyright (c) 2016, 2018 Cirrus Link Solutions and others
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

package org.eclipse.tahu.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tahu.SparkplugParsingException;
import org.eclipse.tahu.message.model.MessageType;
import org.eclipse.tahu.message.model.Topic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides utility methods for handling Sparkplug MQTT message topics.
 */
public class TopicUtil {

	private static final Map<String, String[]> SPLIT_TOPIC_CACHE = new HashMap<String, String[]>();

	public static String[] getSplitTopic(String topic) {
		String[] splitTopic = SPLIT_TOPIC_CACHE.get(topic);
		if (splitTopic == null) {
			splitTopic = topic.split("/");
			SPLIT_TOPIC_CACHE.put(topic, splitTopic);
		}

		return splitTopic;
	}

	/**
	 * Serializes a {@link Topic} instance in to a JSON string.
	 * 
	 * @param topic a {@link Topic} instance
	 * @return a JSON string
	 * @throws JsonProcessingException
	 */
	public static String toJsonString(Topic topic) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(topic);
	}

	/**
	 * Parses a Sparkplug MQTT message topic string and returns a {@link Topic} instance.
	 *
	 * @param topic a topic string
	 * @return a {@link Topic} instance
	 * @throws SparkplugParsingException if an error occurs while parsing
	 */
	public static Topic parseTopic(String topic) throws SparkplugParsingException {
		return parseTopic(TopicUtil.getSplitTopic(topic));
	}

	/**
	 * Parses a Sparkplug MQTT message topic string and returns a {@link Topic} instance.
	 *
	 * @param splitTopic a topic split into tokens
	 * @return a {@link Topic} instance
	 * @throws SparkplugParsingException if an error occurs while parsing
	 */
	@SuppressWarnings("incomplete-switch")
	public static Topic parseTopic(String[] splitTopic) throws SparkplugParsingException {
		MessageType type;
		String namespace, edgeNodeId, groupId;
		int length = splitTopic.length;

		if (length < 4 || length > 5) {
			throw new SparkplugParsingException("Invalid number of topic elements: " + length);
		}

		namespace = splitTopic[0];
		groupId = splitTopic[1];
		type = MessageType.parseMessageType(splitTopic[2]);
		edgeNodeId = splitTopic[3];

		if (length == 4) {
			// A node topic
			switch (type) {
				case STATE:
				case NBIRTH:
				case NCMD:
				case NDATA:
				case NDEATH:
				case NRECORD:
					return new Topic(namespace, groupId, edgeNodeId, type);
			}
		} else {
			// A device topic
			switch (type) {
				case STATE:
				case DBIRTH:
				case DCMD:
				case DDATA:
				case DDEATH:
				case DRECORD:
					return new Topic(namespace, groupId, edgeNodeId, splitTopic[4], type);
			}
		}
		throw new SparkplugParsingException("Invalid number of topic elements " + length + " for topic type " + type);
	}
}
