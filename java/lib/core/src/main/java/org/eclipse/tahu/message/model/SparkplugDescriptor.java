/********************************************************************************
 * Copyright (c) 2020-2022 Cirrus Link Solutions and others
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

public interface SparkplugDescriptor {

	/**
	 * Returns the String representation of this {@link SparkplugDescriptor}
	 *
	 * @return the String representation of this {@link SparkplugDescriptor}
	 */
	public String getDescriptorString();

	/**
	 * Returns the Group ID for this {@link SparkplugDescriptor}
	 *
	 * @return the String representation of the Group ID for this {@link SparkplugDescriptor}
	 */
	public String getGroupId();

	/**
	 * Returns the Group ID for this {@link SparkplugDescriptor}
	 *
	 * @return the String representation of the Group ID for this {@link SparkplugDescriptor}
	 */
	public String getEdgeNodeId();

	/**
	 * Returns true if this is a DeviceDescriptor, otherwise false
	 *
	 * @return true if this is a DeviceDescriptor, otherwise false
	 */
	public boolean isDeviceDescriptor();

	/**
	 * Returns the Group ID for this {@link SparkplugDescriptor}
	 *
	 * @return the String representation of the Group ID for this {@link SparkplugDescriptor}
	 */
	public String getDeviceId();
}
