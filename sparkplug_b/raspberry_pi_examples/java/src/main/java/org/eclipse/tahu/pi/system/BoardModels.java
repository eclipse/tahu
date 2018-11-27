/********************************************************************************
 * Copyright (c) 2018 Cirrus Link Solutions and others
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
package org.eclipse.tahu.pi.system;

/**
 * Enumerates Raspberry Pi board models
 */
public enum BoardModels {
	// Obtained from https://www.raspberrypi.org/documentation/hardware/raspberrypi/revision-codes/README.md
	CODE_900021("900021", Constants.A_PLUS, 1.1f, 512, Constants.SONY_UK),
	CODE_900032("900032", Constants.B_PLUS, 1.2f, 512, Constants.SONY_UK),
	CODE_900092("900092", Constants.ZERO, 1.2f, 512, Constants.SONY_UK),
	CODE_920092("920092", Constants.ZERO, 1.2f, 512, Constants.EMBEST),
	CODE_900093("900093", Constants.ZERO, 1.3f, 512, Constants.SONY_UK),
	CODE_9000C1("9000c1", Constants.ZERO_W, 1.1f, 512, Constants.SONY_UK),
	CODE_920093("920093", Constants.ZERO, 1.3f, 512, Constants.SONY_UK),
	CODE_A01040("a01040", Constants.B2, 1.0f, 10124, Constants.SONY_UK),
	CODE_A01041("a01041", Constants.B2, 1.1f, 1024, Constants.SONY_UK),
	CODE_A02082("a02082", Constants.B3, 1.2f, 1024, Constants.SONY_UK),
	CODE_A020A0("a020a0", Constants.CM3, 1.0f, 1024, Constants.SONY_UK),
	CODE_A21041("a21041", Constants.B2, 1.1f, 1024, Constants.EMBEST),
	CODE_A22042("a22042", Constants.B2_BCM2837, 1.2f, 1024, Constants.EMBEST),
	CODE_A22082("a22082", Constants.B3, 1.2f, 1024, Constants.EMBEST),
	CODE_A32082("a32082", Constants.B3, 1.2f, 1024, Constants.SONY_JAPAN),
	CODE_A52082("a52082", Constants.B3, 1.2f, 1024, Constants.STADIUM),
	CODE_A020D3("a020d3", Constants.B3_PLUS, 1.3f, 10124, Constants.SONY_UK),
	CODE_9020E0("9020e0", Constants.A3_PLUS, 1.0f, 512, Constants.SONY_UK);
	
	private String code;
	private String model;
	private float revision;
	private int ramSize;
	private String manufacturer;
	
	private BoardModels(String code, String model, float revision, int ramSize, String manufacturer) {
		this.code = code;
		this.model = model;
		this.revision = revision;
		this.ramSize = ramSize;
		this.manufacturer = manufacturer;
	}
	
	public String getCode() {
		return code;
	}

	public String getModel() {
		return this.model;
	}

	public float getRevision() {
		return this.revision;
	}

	public int getRamSize() {
		return this.ramSize;
	}

	public String getManufacturer() {
		return this.manufacturer;
	}
	
	private static class Constants {
		private static final String A_PLUS = "A+";
		private static final String B_PLUS = "B+";
		private static final String ZERO = "Zero";
		private static final String ZERO_W = "Zero W";
		private static final String B2 = "2B";
		private static final String B3 = "3B";
		private static final String CM3 = "CM3";
		private static final String B2_BCM2837 = "2B (with BCM2837)";
		private static final String A3_PLUS = "3A+";
		private static final String B3_PLUS = "3B+";
		
		private static final String SONY_UK = "Sony UK";
		private static final String EMBEST = "Embest";
		private static final String SONY_JAPAN = "Sony Japan";
		private static final String STADIUM = "Stadium";
    }
}
