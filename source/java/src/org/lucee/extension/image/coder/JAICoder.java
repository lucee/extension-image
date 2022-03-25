
package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.JAIUtil;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

class JAICoder extends Coder {

	private CFMLEngine enc;

	protected JAICoder() {

	}

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		return JAIUtil.read(res);
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		// System.out.println("JRE.read");
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(bytes, null);

		return JAIUtil.read(new ByteArrayInputStream(bytes), format.equalsIgnoreCase("jpg") ? "JPEG" : format);
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		try {
			JAIUtil.write(img.getBufferedImage(), destination, format);
		}
		catch (PageException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	@Override
	public void write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		try {
			JAIUtil.write(img.getBufferedImage(), os, format);
		}
		catch (PageException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	@Override
	public final String[] getWriterFormatNames() throws IOException {
		throw new IOException("not supported");
	}

	@Override
	public final String[] getReaderFormatNames() throws IOException {
		throw new IOException("not supported");
	}

	@Override
	public boolean supported() {
		return true;
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		throw new IOException("not supported");
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		throw new IOException("not supported");
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		return defaultValue;
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		return defaultValue;
	}
}