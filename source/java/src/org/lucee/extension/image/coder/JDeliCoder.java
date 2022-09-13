package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

public class JDeliCoder extends Coder implements FormatNames, FormatExtract {

	private static final Class<?>[] ARGS_EMPTY = new Class<?>[] {};
	private static final Class<?>[] ARGS_IS = new Class<?>[] { InputStream.class };
	private static final Class<?>[] ARGS_INT = new Class<?>[] { int.class };
	private static final Class<?>[] ARGS_BA = new Class<?>[] { byte[].class };
	private static final Class<?>[] ARGS_WRITE = new Class<?>[] { BufferedImage.class, String.class, OutputStream.class };

	private String[] writerFormatNames = new String[] { "BMP", "HEIC", "JPEG", "JPEG2000", "PNG", "TIFF", "WEBP" };
	private String[] readerFormatNames = new String[] { "BMP", "DICOM", "EMF", "GIF", "HEIC", "ICO", "JPEG", "JPEG2000", "PDF", "PNG", "SGI", "PSD", "TIFF", "WEBP", "WMF" };

	@Override
	public BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		// System.out.println("JDeli.read");
		InputStream is = res.getInputStream();
		try {
			return read(is);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;

			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		// System.out.println("JDeli.read");
		try {
			return read(bytes);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		if (Util.isEmpty(format)) {
			format = getFormat(destination);
			if (Util.isEmpty(format)) format = img.getFormat();
		}
		_write(img, destination.getOutputStream(), format, quality, true, noMeta);
	}

	private void _write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		if (Util.isEmpty(format)) {
			format = img.getFormat();
		}

		boolean isJpeg = ImageUtil.isJPEG(format);
		// System.out.println("JDeli.write");
		try {
			if (!noMeta && isJpeg && img.getSource() != null) {
				throw new IOException("writing metadata to a JPEG file, is not supported by JDeli so far");
			}
			BufferedImage bi = img.getBufferedImage();
			Object options = null;
			if (quality != 0.75 && (format == null || isJpeg)) {
				int q = (int) (quality * 100);
				if (q < 1) q = 1;
				else if (q > 100) q = 100;

				Class<?> clazz = getClazz("com.idrsolutions.image.jpeg.options.JpegEncoderOptions");
				Object inst = clazz.newInstance();
				setQuality(inst, q);
				write(bi, inst, os);
			}
			else write(bi, format, os);

		}
		catch (Throwable e) {
			if (e instanceof ThreadDeath) throw (ThreadDeath) e;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		finally {
			if (closeStream) Util.closeEL(os);
		}
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		if (res.length() == 0) {
			String format = ImageUtil.getFormatFromExtension(res, null);
			if (Util.isEmpty(format)) throw new IOException("not format for extension [" + CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res) + "] found");
			return format;
		}
		return getFormatName(res);
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		return getFormatName(bytes);
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		try {
			return getFormat(res);
		}
		catch (Exception e) {
		}
		return defaultValue;
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		try {
			return getFormat(bytes);
		}
		catch (Exception e) {
		}
		return defaultValue;
	}

	@Override
	public String[] getWriterFormatNames() {
		return writerFormatNames;
	}

	@Override
	public String[] getReaderFormatNames() {
		return readerFormatNames;
	}

	@Override
	public boolean supported() {
		try {
			getClazz("com.idrsolutions.image.JDeli");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}

	private Class<?> getClazz(String className) throws IOException {
		// return com.idrsolutions.image.JDeli.class;
		return CFMLEngineFactory.getInstance().getClassUtil().loadClass(className);
	}

	private BufferedImage read(Object val) throws Throwable {
		try {
			if (val instanceof InputStream) {
				return (BufferedImage) getClazz("com.idrsolutions.image.JDeli").getMethod("read", ARGS_IS).invoke(null, new Object[] { val });
			}
			return (BufferedImage) getClazz("com.idrsolutions.image.JDeli").getMethod("read", ARGS_BA).invoke(null, new Object[] { val });
		}
		catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}

	}

	private void write(BufferedImage bi, Object encoderOptions, OutputStream os) throws Throwable {
		try {

			final Class<?>[] args = new Class<?>[] { BufferedImage.class, getClazz("com.idrsolutions.image.encoder.options.EncoderOptions"), OutputStream.class };
			getClazz("com.idrsolutions.image.JDeli").getMethod("write", args).invoke(null, new Object[] { bi, encoderOptions, os });
		}
		catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}
	}

	private void write(BufferedImage bi, String format, OutputStream os) throws Throwable {
		try {
			getClazz("com.idrsolutions.image.JDeli").getMethod("write", ARGS_WRITE).invoke(null, new Object[] { bi, format, os });
		}
		catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}
	}

	private Object getImageInfo(Object val) throws IOException {
		if (val instanceof Resource || val instanceof InputStream) {
			InputStream is = val instanceof InputStream ? (InputStream) val : ((Resource) val).getInputStream();
			try {
				return getClazz("com.idrsolutions.image.JDeli").getMethod("getImageInfo", ARGS_IS).invoke(null, new Object[] { is });
			}
			catch (RuntimeException re) {
				throw re;
			}
			catch (Throwable t) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
			}
			finally {
				Util.closeEL(is);
			}
		}

		try {
			return getClazz("com.idrsolutions.image.JDeli").getMethod("getImageInfo", ARGS_BA).invoke(null, new Object[] { (byte[]) val });
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
	}

	private Object getImageMetadataType(Object val) throws IOException {
		Object ii = getImageInfo(val);
		if (ii == null) {
			String imageName = "";
			if (val instanceof Resource) imageName = "[" + val + "] ";
			throw new IOException("cannot load metadata for this " + imageName + "image");
		}
		try {
			return ii.getClass().getMethod("getImageMetadataType", ARGS_EMPTY).invoke(ii, new Object[] {});
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
	}

	private void setQuality(Object jpegEncoderOptions, int quality) throws IOException {
		try {
			jpegEncoderOptions.getClass().getMethod("setQuality", ARGS_INT).invoke(jpegEncoderOptions, new Object[] { quality });
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
	}

	private String getFormatName(Object val) throws IOException {
		Object imt = getImageMetadataType(val);
		String raw = imt.toString();
		int index = raw.indexOf('_');
		if (index != -1) return raw.substring(0, index);
		return raw;
	}
}
