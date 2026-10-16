package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

public class AsposeCoder extends Coder implements FormatNames, FormatExtract {

	private static final Class<?>[] ARGS_IS = new Class<?>[] { InputStream.class };

	private static String[] writerFormatNames = new String[] {};
	private static String[] readerFormatNames;
	private static Object token = new Object();

	private static ConcurrentHashMap<Long, String> formats;
	private static Boolean supported;

	public AsposeCoder() {
		if (formats == null) {
			synchronized (token) {
				if (formats == null) {
					try {
						formats = new ConcurrentHashMap<>();

						Class<?> clazz = getClazz("com.aspose.imaging.FileFormat");

						set(clazz, formats, "Bmp");
						set(clazz, formats, "Gif");
						set(clazz, formats, "Dicom");
						set(clazz, formats, "Djvu");
						set(clazz, formats, "Dng");
						set(clazz, formats, "Png");
						set(clazz, formats, "Jpeg");
						set(clazz, formats, "Jpeg2000");
						set(clazz, formats, "Psd");
						set(clazz, formats, "Tiff");
						set(clazz, formats, "Webp");
						set(clazz, formats, "Cdr");
						set(clazz, formats, "Cmx");
						set(clazz, formats, "Emf");
						set(clazz, formats, "Wmf");
						set(clazz, formats, "Svg");
						set(clazz, formats, "Odg");
						set(clazz, formats, "Eps");

						List<String> tmp = new ArrayList<>();
						for (String f: formats.values()) {
							tmp.add(f);
						}
						readerFormatNames = sortAndMerge(tmp.toArray(new String[0]));

						supported = Boolean.TRUE;
					}
					catch (Throwable t) {
						if (t instanceof ThreadDeath) throw (ThreadDeath) t;
						supported = Boolean.FALSE;
					}
				}
			}
		}
	}

	@Override
	public BufferedImage read(Resource res, String format) throws IOException {
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
	public BufferedImage read(byte[] bytes, String format) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			return read(bais);
		}
		catch (RuntimeException re) {
			throw re;
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
		finally {
			Util.closeEL(bais);
		}
	}

	private BufferedImage read(InputStream is) throws Throwable {
		// com.aspose.imaging.RasterImage image = (WebPImage) com.aspose.imaging.Image.load(is);
		// BufferedImage bufferedImage = image.toBitmap();
		try {
			Object objRasterImage = getClazz("com.aspose.imaging.Image").getMethod("load", ARGS_IS).invoke(null, new Object[] { is });
			return (BufferedImage) objRasterImage.getClass().getMethod("toBitmap").invoke(objRasterImage);
		}
		catch (InvocationTargetException ite) {
			throw ite.getTargetException();
		}
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		throw new IOException("encoding not supported by the Aspose Coder");
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		InputStream is = res.getInputStream();
		try {
			return getFormat(is);
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		try {
			return getFormat(bais);
		}
		finally {
			Util.closeEL(bais);
		}
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

	private String getFormat(InputStream is) throws IOException {
		try {
			// com.aspose.imaging.Image.getFileFormat(is);
			Object objFormat = getClazz("com.aspose.imaging.Image").getMethod("getFileFormat", ARGS_IS).invoke(null, new Object[] { is });
			if (objFormat == null) throw new IOException("could not detect format");
			return toFormat(CFMLEngineFactory.getInstance().getCastUtil().toLongValue(objFormat));
		}
		catch (Throwable t) {
			if (t instanceof ThreadDeath) throw (ThreadDeath) t;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(t);
		}
	}

	private String toFormat(long format) throws IOException {
		String f = formats.get(format);
		if (f != null) return f;
		throw new IOException("could not find format for long [" + format + "]");
	}

	private static void set(Class<?> clazz, ConcurrentHashMap<Long, String> formats, String name) throws IOException, RuntimeException {
		try {
			formats.put(clazz.getField(name).getLong(null), name.toUpperCase());
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}

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
		return supported;
	}

	private static Class<?> getClazz(String className) throws IOException {
		return CFMLEngineFactory.getInstance().getClassUtil().loadClass(className);
	}
}
