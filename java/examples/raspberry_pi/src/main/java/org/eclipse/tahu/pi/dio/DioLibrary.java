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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

/**
 * Defines a class that handles libdio.so native library
 */
public class DioLibrary {

	private static final String DFLT_LIBDIO_PATH = "/usr/local/lib/dio";
	private static final String LIBDIO = "/libdio.so";
	private static final String JAVA_LIBRARY_PATH_PROP_NAME = "java.library.path";
	private static final String FAILED_CALCULATE_MD5_MSG = "Failed to calculate MD5 sum";

	private static DioLibrary instance;
	private String libdioPath;

	private DioLibrary() {
		libdioPath = DFLT_LIBDIO_PATH;
	}

	private DioLibrary(String pathToLibdio) {
		libdioPath = pathToLibdio;
	}

	/**
	 * Gets an instance of the DioLibrary class
	 * 
	 * @return an instance of the DioLibrary class as {@link DioLibrary}
	 */
	public static DioLibrary getInstance() {
		if (instance == null) {
			instance = new DioLibrary();
		}
		return instance;
	}

	/**
	 * Gets an instance of the DioLibrary class
	 * 
	 * @param pathToLibdio - path to the libdio.so as {@link String}
	 * @return
	 */
	public static DioLibrary getInstance(String pathToLibdio) {
		if (instance == null) {
			instance = new DioLibrary(pathToLibdio);
		}
		return instance;
	}

	/**
	 * Reports java library path
	 * 
	 * @return java library path as {@link String}
	 */
	public String getJavaLibraryPath() {
		return this.libdioPath;
	}

	/**
	 * Sets java library path
	 * 
	 * @param pathToLibdio - path to the libdio.so as {@link String}
	 * @throws DioException
	 */
	public void setJavaLibraryPath(String pathToLibdio) throws DioException {
		this.libdioPath = pathToLibdio;
		setJavaLibraryPath();
	}

	/**
	 * Sets java library path
	 * 
	 * @throws DioException
	 */
	public void setJavaLibraryPath() throws DioException {
		try {
			Field field = ClassLoader.class.getDeclaredField("usr_paths");
			field.setAccessible(true);
			List<String> paths = new ArrayList<>(Arrays.asList((String[]) field.get(null)));
			paths.forEach(path -> {
				if (this.libdioPath.equals(path)) {
					return;
				}
			});
			paths.add(this.libdioPath);
			field.set(null, paths.stream().toArray(String[]::new));
			addToJavaLibraryPath(this.libdioPath);
		} catch (Exception e) {
			throw new DioException("Failed to set java path to the libdio.so library", e);
		}
	}

	/**
	 * Sets libdio.so native library by copying it from the libdio.so resource if necessary
	 * 
	 * @throws DioException
	 */
	public void setDioLibrary() throws DioException {
		File targetLibdioPath = new File(libdioPath);
		File targetLibdioFile = new File(formAbsolutePathToTargetLibdioFile());
		if (targetLibdioFile.exists()
				&& getMD5sumOfTargetLibdio(targetLibdioFile).equals(getMD5sumOfResourceLibdio())) {
			// the /usr/local/lib/dio/libdio.so file is already in place
			return;
		}
		if (!targetLibdioPath.exists()) {
			targetLibdioPath.mkdirs();
		}
		copyDioLibrary(targetLibdioFile.toPath());
	}

	private void copyDioLibrary(Path path) throws DioException {
		try (InputStream is = getClass().getResourceAsStream(LIBDIO)) {
			Files.copy(is, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw new DioException("Failed to set LIBDIO shared library", e);
		}
	}

	private String getMD5sumOfResourceLibdio() throws DioException {
		String md5;
		try (InputStream is = getClass().getResourceAsStream(LIBDIO)) {
			md5 = getMD5(is);
		} catch (Exception e) {
			throw new DioException("FAILED_CALCULATE_MD5_MSG" + " of libdio.so resource file", e);
		}
		return md5;
	}

	private String getMD5sumOfTargetLibdio(File file) throws DioException {
		String md5;
		try (InputStream is = new FileInputStream(file)) {
			md5 = getMD5(is);
		} catch (Exception e) {
			throw new DioException(FAILED_CALCULATE_MD5_MSG + " of the " + file.getAbsolutePath() + " file", e);
		}
		return md5;
	}

	private String getMD5(InputStream is) throws DioException {
		String md5;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new DioException("Failed to get an instance of MD5", e);
		}
		try (DigestInputStream dis = new DigestInputStream(is, md)) {
			while (dis.available() > 0) {
				dis.read();
			}
			md5 = (new HexBinaryAdapter()).marshal(md.digest());
		} catch (Exception e) {
			throw new DioException(FAILED_CALCULATE_MD5_MSG, e);
		}
		return md5;
	}

	private void addToJavaLibraryPath(String path) {
		StringBuilder sbPath = new StringBuilder().append(System.getProperty(JAVA_LIBRARY_PATH_PROP_NAME))
				.append(File.pathSeparator).append(path);
		System.setProperty(JAVA_LIBRARY_PATH_PROP_NAME, sbPath.toString());
	}

	private String formAbsolutePathToTargetLibdioFile() {
		return new StringBuilder().append(this.libdioPath).append(LIBDIO).toString();
	}
}
