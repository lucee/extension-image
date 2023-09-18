package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageParser;
import org.apache.commons.imaging.formats.jpeg.JpegImagingParameters;
import org.lucee.extension.image.Image;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

class ApacheImagingCoder extends Coder implements FormatNames, FormatExtract {
	private final Map<String, ImageFormat> formats;

	private static String[] readerFormatNames = sortAndMerge(
			new String[] { "BMP", "GIF", "JPEG", "ICNS", "ICO", "PNM", "PGM", "PBM", "PPM", "PNG", "PSD", "TIFF", "WBMP", "XBM", "XPM" });
	private static String[] writerFormatNames = sortAndMerge(new String[] { "BMP", "GIF", "ICNS", "ICO", "PCX", "PNM", "PGM", "PBM", "PPM", "PNG", "TIFF", "WBMP" });

	protected ApacheImagingCoder() {
		super();
		formats = new ConcurrentHashMap<>();
		for (ImageFormat format: ImageFormats.values()) {
			formats.put(format.getName().toUpperCase(), format);
		}

		Imaging.hasImageFileExtension("lucee.gif");// to make sure Sanselan exist when load this class
	}

	/*
	 * static { List<String> readers = new ArrayList<>(); List<String> writers = new ArrayList<>(); for
	 * (ImageFormat format: ImageFormats.values()) { if (format.equals(ImageFormats.UNKNOWN)) continue;
	 * 
	 * byte[] bytes = Images.getImage(format.getName());
	 * 
	 * BufferedImage bi = null;
	 * 
	 * // test readers try { bi = Imaging.getBufferedImage(bytes);
	 * readers.add(format.getName().toUpperCase()); } catch (Exception e) { }
	 * 
	 * // test writers if (bi == null) bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB); try {
	 * Imaging.writeImageToBytes(bi, format); writers.add(format.getName().toUpperCase()); } catch
	 * (Exception e) { }
	 * 
	 * } readerFormatNames = readers.toArray(new String[readers.size()]); writerFormatNames =
	 * readers.toArray(new String[writers.size()]); }
	 */

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(Resource res, String format) throws IOException {
		// System.out.println("Sanselan.read");
		InputStream is = null;
		try {
			return Imaging.getBufferedImage(is = res.getInputStream());
		}
		catch (ImageReadException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		finally {
			Util.closeEL(is);
		}
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage read(byte[] bytes, String format) throws IOException {
		// System.out.println("Sanselan.read");
		try {
			return Imaging.getBufferedImage(new ByteArrayInputStream(bytes));
		}
		catch (ImageReadException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {

		if (Util.isEmpty(format)) {
			format = getFormat(destination, null);
			if (Util.isEmpty(format)) {
				format = img.getFormat();
			}
		}
		ImageFormat imgFor = toFormat(format);

		if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
			OutputStream os = null;
			try {// set optional parameters if you like
				os = destination.getOutputStream();
				final JpegImagingParameters params = new JpegImagingParameters();
				// TODO quality
				new JpegImageParser().writeImage(img.getBufferedImage(), os, params);
				return;
			}
			catch (Exception e) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
			finally {
				Util.closeEL(os);
			}

		}
		OutputStream os = null;
		try {
			Imaging.writeImage(img.getBufferedImage(), os = destination.getOutputStream(), imgFor);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		finally {
			Util.closeEL(os);
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
		return true;
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		long len = res.length();
		if (len > 0) {
			// TODO just copy x bytes
			// if (res instanceof File) return Imaging.guessFormat((File) res).getName();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CFMLEngineFactory.getInstance().getIOUtil().copy(res.getInputStream(), baos, true, true);

			ImageFormat format = Imaging.guessFormat(baos.toByteArray());
			if (format == null || format.equals(ImageFormats.UNKNOWN)) throw new IOException("not matching format found");
			return format.getName();
		}
		else {
			return toFormatByExtension(CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res)).getName();
		}
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		ImageFormat format = Imaging.guessFormat(bytes);
		if (format == null || format.equals(ImageFormats.UNKNOWN)) throw new IOException("not matching format found");
		return format.getName();
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		try {
			return getFormat(res);
		}
		catch (IOException e) {
			return defaultValue;
		}
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		try {
			return getFormat(bytes);
		}
		catch (IOException e) {
			return defaultValue;
		}
	}

	private ImageFormat toFormat(String format, ImageFormat defaultValue) {
		if (Util.isEmpty(format, true)) return defaultValue;
		format = format.toUpperCase().trim();

		ImageFormat f = formats.get(format);
		if (f != null) return f;

		// equals?
		for (ImageFormat imgFor: formats.values()) {
			for (String ext: imgFor.getExtensions()) {
				if (format.equalsIgnoreCase(ext)) return imgFor;
			}
		}
		return defaultValue;
	}

	private ImageFormat toFormat(String format) throws IOException {
		if (Util.isEmpty(format, true)) throw new IOException("missing format defintion");
		format = format.toUpperCase().trim();

		ImageFormat f = formats.get(format);
		if (f != null) return f;

		// equals?
		for (ImageFormat imgFor: formats.values()) {
			for (String ext: imgFor.getExtensions()) {
				if (format.equalsIgnoreCase(ext)) return imgFor;
			}
		}
		throw new IOException("no matching encoder for format [" + format + "] in apache imaging suite found");
	}

	private ImageFormat toFormatByExtension(String ext) throws IOException {
		if (Util.isEmpty(ext, true)) throw new IOException("missing extension defintion");
		ext = ext.toUpperCase().trim();

		// equals?
		for (ImageFormat imgFor: formats.values()) {
			for (String ex: imgFor.getExtensions()) {
				if (ext.equalsIgnoreCase(ex)) return imgFor;
			}
		}
		throw new IOException("no matching format for extension [" + ext + "] in apache imaging suite found");
	}

}