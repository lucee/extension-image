package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

class ImageIOCoder extends Coder implements FormatNames, FormatExtract {

	protected ImageIOCoder() {
		super();
	}

	@Override
	public final BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		InputStream is = null;
		try {
			return ImageIO.read(is = res.getInputStream());
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public final BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		return ImageIO.read(new ByteArrayInputStream(bytes));

	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		if (Util.isEmpty(format)) {
			format = getFormat(destination);
			if (Util.isEmpty(format)) format = img.getFormat();
		}

		ImageOutputStream ios = null;
		OutputStream os = null;

		if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
			try {
				os = destination.getOutputStream();
				ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
				ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
				jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				jpgWriteParam.setCompressionQuality(quality);

				ios = ImageIO.createImageOutputStream(os);
				jpgWriter.setOutput(ios);
				IIOImage outputImage = new IIOImage(img.getBufferedImage(), null, null);
				jpgWriter.write(null, outputImage, jpgWriteParam);
				jpgWriter.dispose();
				return;
			}
			catch (Exception e) {
				os = null;
				Log log = Coder.log();
				if (log != null) log.error("image", e);
				else e.printStackTrace();
			}
			finally {
				try {
					if (os != null) os.flush();
				}
				catch (IOException ioe) {
				}
				try {
					if (ios != null) ios.close();
				}
				catch (IOException ioe) {
				}

				Util.closeEL(os);
			}
		}

		try {
			os = destination.getOutputStream();
			ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(img.getBufferedImage(), format, ios);
		}
		catch (PageException pe) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(pe);
		}
		finally {
			os.flush();
			ios.close();
			Util.closeEL(os);
		}

		if (destination.length() == 0) throw new IOException("could not encode to format [" + format + "]");

	}

	@Override
	public boolean supported() {
		try {
			getClass().getClassLoader().loadClass("javax.imageio.event.IIOReadProgressListener");
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			return false;
		}
		return true;
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		long len = res.length();
		if (len > 0) {
			return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null));
		}
		else {
			String format = ImageUtil.getFormatFromExtension(res, null);
			if (!Util.isEmpty(format)) return format;
		}
		throw new IOException("cannot guess format from an empty file");
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		long len = res.length();
		if (len > 0) {
			return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null), defaultValue);
		}
		return defaultValue;
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(bytes, null));
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(bytes, null), defaultValue);
	}

	private String getFormatbyMimeType(String mimeType, String defaultValue) {
		if (!Util.isEmpty(mimeType)) {
			try {
				return getFormatbyMimeType(mimeType);
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			}
		}
		return defaultValue;
	}

	private String getFormatbyMimeType(String mimeType) throws IOException {
		if (!Util.isEmpty(mimeType)) {
			Iterator<ImageReader> it = ImageIO.getImageReadersByMIMEType(mimeType);
			while (it != null && it.hasNext()) {
				String fn = it.next().getFormatName();
				return fn;
			}

		}
		throw new IOException("no matching format found for mimetype [" + mimeType + "]");
	}

	@Override
	public final String[] getWriterFormatNames() {
		return ImageIO.getWriterFormatNames();
	}

	@Override
	public final String[] getReaderFormatNames() {
		return ImageIO.getReaderFormatNames();
	}

}