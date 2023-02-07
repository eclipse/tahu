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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * A class to represent the meta data associated with a metric.
 */
@JsonInclude(Include.NON_NULL)
public class MetaData {

	/**
	 * Indicates if the metric represents one of multiple parts.
	 */
	private Boolean isMultiPart;

	/**
	 * A content type associated with the metric.
	 */
	private String contentType;

	/**
	 * A size associated with the metric.
	 */
	private Long size;

	/**
	 * A sequence associated with the metric.
	 */
	private Long seq;

	/**
	 * A file name associated with the metric.
	 */
	private String fileName;

	/**
	 * A file type associated with the metric.
	 */
	private String fileType;

	/**
	 * A MD5 sum associated with the metric.
	 */
	private String md5;

	/**
	 * A description associated with the metric.
	 */
	private String description;

	/**
	 * Default no-arg constructor.
	 */
	public MetaData() {
	}

	/**
	 * Constructor with fields.
	 * 
	 * @param isMultiPart if the metric represents one of multiple parts.
	 * @param contentType a content type associated with the metric.
	 * @param size a size associated with the metric.
	 * @param seq a sequence associated with the metric.
	 * @param fileName a file name associated with the metric.
	 * @param fileType a file type associated with the metric.
	 * @param md5 a MD5 sum associated with the metric.
	 * @param description a description associated with the metric
	 */
	public MetaData(Boolean isMultiPart, String contentType, Long size, Long seq, String fileName, String fileType,
			String md5, String description) {
		this.isMultiPart = isMultiPart;
		this.contentType = contentType;
		this.size = size;
		this.seq = seq;
		this.fileName = fileName;
		this.fileType = fileType;
		this.md5 = md5;
		this.description = description;
	}

	/**
	 * Copy Constructor
	 *
	 * @param metaData the {@link MetaData} to copy
	 */
	public MetaData(MetaData metaData) {
		this(metaData.isMultiPart(), metaData.getContentType(), metaData.getSize(), metaData.getSeq(),
				metaData.getFileName(), metaData.getFileType(), metaData.getMd5(), metaData.getDescription());
	}

	/**
	 * Whether or not this is a mult-part {@link MetaData}
	 *
	 * @return true if this is multi-part {@link MetaData} otherwise false
	 */
	public Boolean isMultiPart() {
		return isMultiPart;
	}

	/**
	 * Sets whether or not this is multi-part {@link MetaData}
	 *
	 * @param isMultiPart whether or not this is multi-part {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setMultiPart(Boolean isMultiPart) {
		this.isMultiPart = isMultiPart;
		return this;
	}

	/**
	 * Gets the ContentType of this {@link MetaData}
	 *
	 * @return the ContentType of this {@link MetaData}
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the ContentType of this {@link MetaData}
	 *
	 * @param contentType the ContentType of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setContentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	/**
	 * Gets the size of this {@link MetaData}
	 *
	 * @return the size of this {@link MetaData}
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * Sets the size of this {@link MetaData}
	 *
	 * @param size the size of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setSize(Long size) {
		this.size = size;
		return this;
	}

	/**
	 * Gets the sequence number of this {@link MetaData}
	 *
	 * @return the sequence number of this {@link MetaData}
	 */
	public Long getSeq() {
		return seq;
	}

	/**
	 * Sets the sequence number of this {@link MetaData}
	 *
	 * @param seq the sequence number of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setSeq(Long seq) {
		this.seq = seq;
		return this;
	}

	/**
	 * Gets the filename of this {@link MetaData}
	 *
	 * @return the filename of this {@link MetaData}
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the filename of this {@link MetaData}
	 *
	 * @param fileName the filename of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setFileName(String fileName) {
		this.fileName = fileName;
		return this;
	}

	/**
	 * Gets the file type of this {@link MetaData}
	 *
	 * @return the file type of this {@link MetaData}
	 */
	public String getFileType() {
		return fileType;
	}

	/**
	 * Sets the file type of this {@link MetaData}
	 *
	 * @param fileType the file type of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setFileType(String fileType) {
		this.fileType = fileType;
		return this;
	}

	/**
	 * Gets the MD5 sum of this {@link MetaData}
	 *
	 * @return the MD5 sum of this {@link MetaData}
	 */
	public String getMd5() {
		return md5;
	}

	/**
	 * Sets the MD5 sum of this {@link MetaData}
	 *
	 * @param md5 the MD% sum of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setMd5(String md5) {
		this.md5 = md5;
		return this;
	}

	/**
	 * Gets the description of this {@link MetaData}
	 *
	 * @return the description of this {@link MetaData}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of this {@link MetaData}
	 *
	 * @param description the description of this {@link MetaData}
	 *
	 * @return the {@link MetaData} that was just modified
	 */
	public MetaData setDescription(String description) {
		this.description = description;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MetaData [isMultiPart=");
		builder.append(isMultiPart);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append(", size=");
		builder.append(size);
		builder.append(", seq=");
		builder.append(seq);
		builder.append(", fileName=");
		builder.append(fileName);
		builder.append(", fileType=");
		builder.append(fileType);
		builder.append(", md5=");
		builder.append(md5);
		builder.append(", description=");
		builder.append(description);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (object == null || this.getClass() != object.getClass()) {
			return false;
		}
		MetaData meta = (MetaData) object;
		return Objects.equals(isMultiPart, meta.isMultiPart()) && Objects.equals(contentType, meta.getContentType())
				&& Objects.equals(size, meta.getSize()) && Objects.equals(seq, meta.getSeq())
				&& Objects.equals(fileName, meta.getFileName()) && Objects.equals(fileType, meta.getFileType())
				&& Objects.equals(md5, meta.getMd5()) && Objects.equals(description, meta.getDescription());
	}

	/**
	 * A Builder for a MetaData instance.
	 */
	public static class MetaDataBuilder {

		private Boolean isMultiPart;
		private String contentType;
		private Long size;
		private Long seq;
		private String fileName;
		private String fileType;
		private String md5;
		private String description;

		public MetaDataBuilder() {
		};

		public MetaDataBuilder(MetaData metaData) {
			this.isMultiPart = metaData.isMultiPart();
			this.contentType = metaData.getContentType();
			this.size = metaData.getSize();
			this.seq = metaData.getSeq();
			this.fileName = metaData.getFileName();
			this.fileType = metaData.getFileType();
			this.md5 = metaData.getMd5();
			this.description = metaData.getDescription();
		}

		public MetaDataBuilder multiPart(Boolean isMultiPart) {
			this.isMultiPart = isMultiPart;
			return this;
		}

		public MetaDataBuilder contentType(String contentType) {
			this.contentType = contentType;
			return this;
		}

		public MetaDataBuilder size(Long size) {
			this.size = size;
			return this;
		}

		public MetaDataBuilder seq(Long seq) {
			this.seq = seq;
			return this;
		}

		public MetaDataBuilder fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}

		public MetaDataBuilder fileType(String fileType) {
			this.fileType = fileType;
			return this;
		}

		public MetaDataBuilder md5(String md5) {
			this.md5 = md5;
			return this;
		}

		public MetaDataBuilder description(String description) {
			this.description = description;
			return this;
		}

		public MetaData createMetaData() {
			return new MetaData(isMultiPart, contentType, size, seq, fileName, fileType, md5, description);
		}
	}
}
