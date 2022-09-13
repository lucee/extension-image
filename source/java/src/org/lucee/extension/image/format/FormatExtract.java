package org.lucee.extension.image.format;

import java.io.IOException;

import lucee.commons.io.res.Resource;

public interface FormatExtract {
	public abstract String getFormat(Resource res) throws IOException;

	public abstract String getFormat(Resource res, String defaultValue);

	public abstract String getFormat(byte[] bytes) throws IOException;

	public abstract String getFormat(byte[] bytes, String defaultValue);
}
