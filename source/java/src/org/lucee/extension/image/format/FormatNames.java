package org.lucee.extension.image.format;

import java.io.IOException;

public interface FormatNames {

	public abstract String[] getWriterFormatNames() throws IOException;

	public abstract String[] getReaderFormatNames() throws IOException;
}
