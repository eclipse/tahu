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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines Raspberry Pi system info
 */
public class SystemInfo {

	private static final String OS_FW_BUILD_KEY = "getOsFirmwareBuild";
	private static final String HARDWARE_KEY = "Hardware";
	private static final String REVISION_KEY = "Revision";
	private static final String SERIAL_NUM_KEY = "Serial";
	private static final String BOARD_MODEL_KEY = "BoardModel";
	private static final String BOARD_MANUFACTURER_KEY = "BoardManufacturer";

	private static SystemInfo instance;

	private Map<String, String> sysInfo;
	private Map<String, BoardModels> boardModelInfo;

	private SystemInfo() throws SystemInfoException {
		this.sysInfo = new HashMap<>();
		this.boardModelInfo = new HashMap<>();
		populateBoardModels();
		getSystemInfo();
	}

	/**
	 * Gets an instance of the SystemInfo class
	 * 
	 * @return instance of the SystemInfo class as {@link SystemInfo}
	 * @throws SystemInfoException
	 */
	public static SystemInfo getInstance() throws SystemInfoException {
		if (instance == null) {
			instance = new SystemInfo();
		}
		return instance;
	}

	/**
	 * Reports OS Firmware build
	 * 
	 * @return OS Firmware build as {@link String}
	 */
	public String getOsFirmwareBuild() {
		String osFirmwareVersion;
		osFirmwareVersion = this.sysInfo.get(OS_FW_BUILD_KEY);
		if (osFirmwareVersion == null) {
			osFirmwareVersion = "";
		}
		return osFirmwareVersion;
	}

	/**
	 * Reports Raspberry Pi model
	 * 
	 * @return model as {@link String}
	 */
	public String getModel() {
		String boardModel;
		boardModel = this.sysInfo.get(BOARD_MODEL_KEY);
		if (boardModel == null) {
			boardModel = "";
		}
		return boardModel;
	}

	/**
	 * Reports Raspberry Pi manufacturer
	 * 
	 * @return manufacturer as {@link String}
	 */
	public String getManufacturer() {
		String boardManufacturer;
		boardManufacturer = this.sysInfo.get(BOARD_MANUFACTURER_KEY);
		if (boardManufacturer == null) {
			boardManufacturer = "";
		}
		return boardManufacturer;
	}

	/**
	 * Reports Raspberry Pi hardware information
	 * 
	 * @return hardware information as {@link String}
	 */
	public String getHardware() {
		String hardware;
		hardware = this.sysInfo.get(HARDWARE_KEY);
		if (hardware == null) {
			hardware = "";
		}
		return hardware;
	}

	/**
	 * Reports revision
	 * 
	 * @return revision as {@link String}
	 */
	public String getRevision() {
		String revision;
		revision = this.sysInfo.get(REVISION_KEY);
		if (revision == null) {
			revision = "";
		}
		return revision;
	}

	private void populateBoardModels() {
		boardModelInfo.put(BoardModels.CODE_900021.getCode(), BoardModels.CODE_900021);
		boardModelInfo.put(BoardModels.CODE_900032.getCode(), BoardModels.CODE_900032);
		boardModelInfo.put(BoardModels.CODE_900092.getCode(), BoardModels.CODE_900092);
		boardModelInfo.put(BoardModels.CODE_920092.getCode(), BoardModels.CODE_920092);
		boardModelInfo.put(BoardModels.CODE_900093.getCode(), BoardModels.CODE_900093);
		boardModelInfo.put(BoardModels.CODE_9000C1.getCode(), BoardModels.CODE_9000C1);
		boardModelInfo.put(BoardModels.CODE_920093.getCode(), BoardModels.CODE_920093);
		boardModelInfo.put(BoardModels.CODE_A01040.getCode(), BoardModels.CODE_A01040);
		boardModelInfo.put(BoardModels.CODE_A01041.getCode(), BoardModels.CODE_A01041);
		boardModelInfo.put(BoardModels.CODE_A02082.getCode(), BoardModels.CODE_A02082);
		boardModelInfo.put(BoardModels.CODE_A020A0.getCode(), BoardModels.CODE_A020A0);
		boardModelInfo.put(BoardModels.CODE_A21041.getCode(), BoardModels.CODE_A21041);
		boardModelInfo.put(BoardModels.CODE_A22042.getCode(), BoardModels.CODE_A22042);
		boardModelInfo.put(BoardModels.CODE_A22082.getCode(), BoardModels.CODE_A22082);
		boardModelInfo.put(BoardModels.CODE_A32082.getCode(), BoardModels.CODE_A32082);
		boardModelInfo.put(BoardModels.CODE_A52082.getCode(), BoardModels.CODE_A52082);
		boardModelInfo.put(BoardModels.CODE_A020D3.getCode(), BoardModels.CODE_A020D3);
		boardModelInfo.put(BoardModels.CODE_9020E0.getCode(), BoardModels.CODE_9020E0);
	}

	private void getSystemInfo() throws SystemInfoException {
		try (FileReader fr = new FileReader("/proc/cpuinfo"); BufferedReader br = new BufferedReader(fr)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith(HARDWARE_KEY)) {
					this.sysInfo.put(HARDWARE_KEY, line.substring(line.indexOf(':') + 1).trim());
				} else if (line.startsWith(REVISION_KEY)) {
					String revision = line.substring(line.indexOf(':') + 1).trim();
					this.sysInfo.put(REVISION_KEY, revision);
					BoardModels boardModel = this.boardModelInfo.get(revision);
					this.sysInfo.put(BOARD_MODEL_KEY, boardModel.getModel());
					this.sysInfo.put(BOARD_MANUFACTURER_KEY, boardModel.getManufacturer());
				} else if (line.startsWith(SERIAL_NUM_KEY)) {
					this.sysInfo.put(SERIAL_NUM_KEY, line.substring(line.indexOf(':') + 1).trim());
				}
			}
		} catch (Exception e) {
			throw new SystemInfoException("failed to obtain system info", e);
		}
		getOsFirmwareVersion();
	}

	private void getOsFirmwareVersion() throws SystemInfoException {
		Process p;
		try {
			p = Runtime.getRuntime().exec("/opt/vc/bin/vcgencmd version");
			p.waitFor();
		} catch (Exception e) {
			throw new SystemInfoException("failed to obtain OS FW Version info", e);
		}
		try (InputStreamReader isr = new InputStreamReader(p.getInputStream());
				BufferedReader br = new BufferedReader(isr)) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.startsWith("version")) {
					this.sysInfo.put(OS_FW_BUILD_KEY, line.substring("version".length()).trim());
					break;
				}
			}
		} catch (Exception e) {
			throw new SystemInfoException("failed to parse OS FW Version info", e);
		}
	}
}
