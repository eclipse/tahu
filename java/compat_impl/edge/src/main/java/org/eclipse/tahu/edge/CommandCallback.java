/*
 * Licensed Materials - Property of Cirrus Link Solutions
 * Copyright (c) 2022 Cirrus Link Solutions LLC - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */
package org.eclipse.tahu.edge;

public interface CommandCallback {

	public void setDeviceOffline(String deviceId);

	public void setDeviceOnline(String deviceId);
}
