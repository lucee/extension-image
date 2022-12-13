
package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.JAIUtil;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

class JAICoder extends Coder {

	protected JAICoder() {

	}

	@Override
	public final BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		return JAIUtil.read(res);
	}

	@Override
	public final BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		return JAIUtil.read(new ByteArrayInputStream(bytes), format);
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		if (Util.isEmpty(format)) {
			format = ImageUtil.getFormatFromExtension(destination, null);
			if (Util.isEmpty(format)) format = img.getFormat();
		}
		try {
			JAIUtil.write(img.getBufferedImage(), destination, format);
		}
		catch (PageException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}
	/*
	 * public void write(Image img, OutputStream os, String format, float quality, boolean closeStream,
	 * boolean noMeta) throws IOException { if (Util.isEmpty(format)) format = img.getFormat(); try {
	 * JAIUtil.write(img.getBufferedImage(), os, format); } catch (PageException e) { throw
	 * CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e); } }
	 */

	@Override
	public boolean supported() {
		return true;
	}
}