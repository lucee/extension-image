package org.lucee.extension.image.format;

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

/**
 * Optional capability for coders that can provide a configured {@link ImageReader}
 * without decoding pixels. Enables lazy metadata paths (width/height/colormodel)
 * to use coder-specific SPIs without requiring them to be registered with the
 * JVM-global {@link javax.imageio.spi.IIORegistry}.
 *
 * Implementations return null for formats they do not handle — callers should
 * check for null and fall through.
 *
 * Callers are responsible for disposing the returned reader.
 */
public interface LazyReader {

	ImageReader getReader(String format, ImageInputStream iis) throws IOException;
}
