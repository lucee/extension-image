package org.lucee.extension.image.format;

import java.io.IOException;

import lucee.commons.io.res.Resource;

public interface FormatExtract {
	public abstract String getFormat(Resource res) throws IOException;

	public abstract String getFormat(Resource res, String defaultValue);

	public abstract String getFormat(byte[] bytes) throws IOException;

	public abstract String getFormat(byte[] bytes, String defaultValue);

	// overloads that accept a pre-resolved MIME type to avoid repeated Tika detection
	default String getFormat(Resource res, String mimeType, String defaultValue) {
		return getFormat(res, defaultValue);
	}

	default String getFormat(byte[] bytes, String mimeType, String defaultValue) {
		return getFormat(bytes, defaultValue);
	}
}
