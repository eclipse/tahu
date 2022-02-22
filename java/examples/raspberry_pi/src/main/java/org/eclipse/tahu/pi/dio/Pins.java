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
package org.eclipse.tahu.pi.dio;

/**
 * Enumerates Raspbery Pi pins
 */
public enum Pins {
	P3(2, "GPIO2 (SDA1, I2C)"),
	P5(3, "GPIO3 (SCL1, I2C)"),
	P7(4, "GPIO4 (GPIO_GCLK)"),
	P8(14, "GPIO14 (TXD0)"),
	P10(15, "GPIO15 (RXD0)"),
	P11(17, "GPIO17 (GPIO_GEN0)"),
	P12(18, "GPIO18 (GPIO_GEN1)"),
	P13(27, "GPIO27 (GPIO_GEN2)"),
	P15(22, "GPIO22 (GPIO_GEN3)"),
	P16(23, "GPIO23 (GPIO_GEN4)"),
	P18(24, "GPIO24 (GPIO_GEN5)"),
	P19(10, "GPIO10, (SPI_MOSI)"),
	P21(9, "GPIO9, (SPI_MISO)"),
	P22(25, "GPIO25, (GPIO_GEN6)"),
	P23(11, "GPIO11 (SPI_CLK)"),
	P24(8, "GPIO8 (SPI_CE0_N)"),
	P26(7, "GPIO7 (SPI_CE1_N)"),
	P27(0, "ID_SD (I2C ID EEPROM)"),
	P28(1, "ID_SC (I2C ID EEPROM)"),
	P29(5, "GPIO5"),
	P31(6, "GPIO6"),
	P32(12, "GPIO12"),
	P33(13, "GPIO13"),
	P35(19, "GPIO19"),
	P36(16, "GPIO16"),
	P337(26, "GPIO26"),
	P38(20, "GPIO20"),
	P40(21, "GPIO21");
	
	private int gpio;
	private String name;
	
	private Pins (int gpio, String name) {
		this.gpio = gpio;
		this.name = name;
	}
	
	public int getGPIO() {
		return this.gpio;
	}
	
	public String getName() {
		return this.name;
	}
}
