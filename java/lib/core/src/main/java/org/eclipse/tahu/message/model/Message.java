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

/**
 * A class to represent a Message.
 */
public class Message {

	private Topic topic;

	private SparkplugBPayload payload;

	/**
	 * Default Constructor
	 */
	public Message() {
	}

	/**
	 * Copy Constructor
	 *
	 * @param message the {@link Message} to copy
	 */
	public Message(Message message) {
		this.topic = message.getTopic();
		this.payload = new SparkplugBPayload(message.getPayload());
	}

	/**
	 * Constructor
	 *
	 * @param topic the {@link Topic} associated with the {@link Message}
	 * @param payload the {@link SparkplugBPayload} associated with the {@link Message}
	 */
	private Message(Topic topic, SparkplugBPayload payload) {
		super();
		this.topic = topic;
		this.payload = payload;
	}

	/**
	 * Gets the {@link Topic} of this {@link Message}
	 *
	 * @return the {@link Topic} of this {@link Message}
	 */
	public Topic getTopic() {
		return topic;
	}

	/**
	 * Gets the {@link SparkplugBPayload} of this {@link Message}
	 *
	 * @return the {@link SparkplugBPayload} of this {@link Message}
	 */
	public SparkplugBPayload getPayload() {
		return payload;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [topic=");
		builder.append(topic);
		builder.append(", payload=");
		builder.append(payload);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * A builder for creating a {@link SparkplugBPayload} instance.
	 */
	public static class MessageBuilder {

		private Topic topic;

		private SparkplugBPayload payload;

		public MessageBuilder(Topic topic, SparkplugBPayload payload) {
			this.topic = topic;
			this.payload = payload;
		}

		public MessageBuilder() {
		}

		public MessageBuilder topic(Topic topic) {
			this.topic = topic;
			return this;
		}

		public MessageBuilder payload(SparkplugBPayload payload) {
			this.payload = payload;
			return this;
		}

		public Message build() {
			return new Message(this.topic, this.payload);
		}
	}
}
