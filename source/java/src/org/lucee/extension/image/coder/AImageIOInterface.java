package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;
import org.lucee.extension.image.util.print;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.config.Config;

public abstract class AImageIOInterface extends Coder implements FormatNames, FormatExtract {

	private static final Class<?>[] READ_CONSTR0 = new Class[] {};
	private static final Class<?>[] READ_CONSTR1 = new Class[] { ImageReaderSpi.class };
	private static final Class<?>[] READ_CONSTR2 = new Class[] { ImageReaderSpi.class, ImageReader.class };

	private static final Class<?>[] WRITE_CONSTR0 = new Class[] {};
	private static final Class<?>[] WRITE_CONSTR1 = new Class[] { ImageWriterSpi.class };
	private static final Class<?>[] WRITE_CONSTR2 = new Class[] { ImageWriterSpi.class, ImageWriter.class };

	protected final Map<String, Codec> codecs = new ConcurrentHashMap<>();

	private String[] writerFormatNames;
	private String[] readerFormatNames;
	private boolean supported = true;

	public AImageIOInterface() {

	}

	public void init() {

		// set formatNames
		List<String> readers = new ArrayList<>();
		List<String> writers = new ArrayList<>();
		for (Map.Entry<String, Codec> e: codecs.entrySet()) {
			if (e.getValue().hasReader()) readers.add(e.getKey());
			if (e.getValue().hasWriter()) writers.add(e.getKey());
		}
		readerFormatNames = sortAndMerge(readers.toArray(new String[0]));
		writerFormatNames = sortAndMerge(writers.toArray(new String[0]));

		print.e(readerFormatNames);
		print.e(writerFormatNames);
	}

	@Override
	public BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		if (res instanceof File) return createBufferedImage(res, format);
		InputStream is = null;
		try {
			is = res.getInputStream();
			return createBufferedImage(is, format);
		}
		catch (LinkageError le) {// can throw NoClassDefFoundError
			supported = false;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(le);
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			return createBufferedImage(bais, format);
		}
		catch (LinkageError le) {// can throw NoClassDefFoundError
			supported = false;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(le);
		}
		finally {
			Util.closeEL(bais);
		}
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		if (destination instanceof File) writeImage(img, destination, format, quality, noMeta);
		OutputStream os = null;
		try {
			os = destination.getOutputStream();
			writeImage(img, os, format, quality, noMeta);
		}
		catch (LinkageError le) {// can throw NoClassDefFoundError
			supported = false;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(le);
		}
		finally {
			Util.closeEL(os);
		}
	}

	@Override
	public String[] getWriterFormatNames() throws IOException {
		return writerFormatNames;
	}

	@Override
	public String[] getReaderFormatNames() throws IOException {
		return readerFormatNames;
	}

	@Override
	public boolean supported() {
		return supported;
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		long len = res.length();
		if (len > 0) {
			return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null));
		}
		String ext = CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res, null);
		if (!Util.isEmpty(ext)) {
			return getFormatByExtension(ext);
		}

		throw new IOException("cannot guess the format from an empty file");
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {

		long len = res.length();
		if (len > 0) {
			return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null), defaultValue);
		}
		String ext = CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res, null);
		if (!Util.isEmpty(ext)) {
			return getFormatByExtension(ext, defaultValue);
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

			for (Map.Entry<String, Codec> e: codecs.entrySet()) {
				for (String mt: e.getValue().mimeTypes) {

					if (mimeType.equalsIgnoreCase(mt)) {
						return e.getKey();
					}
				}
			}
		}
		throw new IOException("no matching format found for mimetype [" + mimeType + "]");
	}

	private void writeImage(Image img, Object output, String format, float quality, boolean noMeta) throws IOException {
		// MUST quality

		Codec[] arr;
		if (!Util.isEmpty(format, true)) {
			Codec codec = codecs.get(format.toUpperCase());
			if (codec == null || !codec.hasWriter()) throw new IOException("format [" + format + "] not supported for writing");
			arr = new Codec[] { codec };
		}
		else {
			arr = codecs.values().toArray(new Codec[0]);
		}

		javax.imageio.stream.ImageOutputStream stream = ImageIO.createImageOutputStream(output);
		ImageWriter writer = null;
		Exception ex = null;
		try {
			for (Codec codec: arr) {
				if (!codec.hasWriter()) continue;
				try {
					ImageWriterSpi spi;
					if (codec.writerSpi != null) {
						spi = codec.writerSpi.newInstance();
						writer = spi.createWriterInstance();
					}
					else {
						ImageWriterSpiImpl writerSpi = loadWriterSpi(codec._writer, codec);
						writer = writerSpi.createWriterInstance();
						writerSpi.setType(stream.getClass());
					}

					ImageWriteParam param = writer.getDefaultWriteParam();

					// param.setCompressionQuality(quality);
					// param.setCompressionMode(ImageWriteParam.MODE_COPY_FROM_METADATA);
					writer.setOutput(stream);
					IIOMetadata meta = noMeta ? null : img.getMetaData(null, format);
					try {
						writer.write(meta, new IIOImage(img.getBufferedImage(), null, null), param);
					}
					catch (IIOException iioe) {
						/*
						 * print.e(iioe.getMessage()); if (iioe.getMessage().indexOf("Only TYPE_4BYTE_ABGR supported") !=
						 * -1) { print.e("try again:" + img.getBufferedImage().getType());
						 * 
						 * writer.write(meta, new IIOImage(ImageUtil.convertColorspace(img.getBufferedImage(),
						 * BufferedImage.TYPE_4BYTE_ABGR), null, null), param); } else
						 */ throw iioe;
					}
					return;
				}
				catch (Exception e) {
					ex = e;

				}
				finally {
					if (writer != null) writer.dispose();
					stream.flush();
				}
			}
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {
				}
			}
		}
		if (ex != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(ex);
	}

	private String getFormatByExtension(String ext, String defaultValue) {
		if (Util.isEmpty(ext)) return defaultValue;
		for (Codec c: codecs.values()) {
			if (c.suffixes == null || c.suffixes.length == 0 || c.formatNames == null || c.formatNames.length == 0) continue;
			for (String sufx: c.suffixes) {
				if (ext.equalsIgnoreCase(sufx)) return c.formatNames[0];
			}
		}
		return defaultValue;
	}

	private String getFormatByExtension(String ext) throws IOException {
		if (Util.isEmpty(ext)) throw new IOException("extension is empty");
		for (Codec c: codecs.values()) {
			if (c.suffixes == null || c.suffixes.length == 0 || c.formatNames == null || c.formatNames.length == 0) continue;
			for (String sufx: c.suffixes) {
				if (ext.equalsIgnoreCase(sufx)) return c.formatNames[0];
			}
		}
		throw new IOException("no format for extension [" + ext + "] found in the TwelveMonkeysCoder");
	}

	private BufferedImage createBufferedImage(Object input, String format) throws IOException {
		Codec[] arr;
		if (!Util.isEmpty(format, true)) {

			Codec codec = codecs.get(format.toUpperCase());
			if (codec == null || !codec.hasReader()) throw new IOException("format [" + format + "] not supported for reading");
			arr = new Codec[] { codec };
		}
		else {
			arr = codecs.values().toArray(new Codec[0]);
		}

		javax.imageio.stream.ImageInputStream stream = ImageIO.createImageInputStream(input);

		BufferedImage bi;
		ImageReader reader = null;
		IOException ioe = null;
		try {
			for (Codec codec: arr) {
				if (!codec.hasReader()) continue;
				try {
					ImageReaderSpi spi;
					ImageReadParam param;
					if (codec.readerSpi != null) {
						spi = codec.readerSpi.newInstance();
						reader = spi.createReaderInstance();
						param = reader.getDefaultReadParam();

					}
					else {
						ImageReaderSpiImpl readerSpi = loadReaderSpi(codec._reader, codec);
						reader = readerSpi.createReaderInstance();
						param = reader.getDefaultReadParam();
						readerSpi.setType(stream.getClass());
					}

					reader.setInput(stream, true, true);
					bi = reader.read(0, param);
					if (bi != null) return bi;
				}
				catch (Exception e) {
					ioe = CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);

				}
				finally {
					if (reader != null) reader.dispose();
				}
			}
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {
				}
			}
		}
		if (ioe != null) throw ioe;
		return null;
	}

	private static Class<? extends ImageReader> loadReader(ClassLoader cl, String className) throws IOException {

		try {
			return (Class<? extends ImageReader>) cl.loadClass(className);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	private static ImageReaderSpiImpl loadReaderSpi(Class<? extends ImageReader> clazz, Codec codec) throws IOException {
		try {
			return new ImageReaderSpiImpl(clazz, codec);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	private static Class<? extends ImageWriter> loadWriter(ClassLoader cl, String className) throws IOException {

		try {
			return (Class<? extends ImageWriter>) cl.loadClass(className);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	private static ImageWriterSpiImpl loadWriterSpi(Class<? extends ImageWriter> clazz, Codec codec) throws IOException {

		try {
			return new ImageWriterSpiImpl(clazz, codec);
		}
		catch (Exception e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	protected static class Codec {
		private final String[] formatNames;
		private final String[] suffixes;
		private final String[] mimeTypes;
		private final Class<? extends ImageReader> _reader;
		private final Class<? extends ImageWriter> _writer;
		private final Class<? extends ImageReaderSpi> readerSpi;
		private final Class<? extends ImageWriterSpi> writerSpi;

		public Codec(String[] formatNames, String[] suffixes, String[] mimeTypes, Class<? extends ImageReader> reader, Class<? extends ImageWriter> writer,
				Class<? extends ImageReaderSpi> readerSpi, Class<? extends ImageWriterSpi> writerSpi) {
			super();
			this.formatNames = formatNames == null ? new String[0] : formatNames;
			this.suffixes = suffixes == null ? new String[0] : suffixes;
			this.mimeTypes = mimeTypes == null ? new String[0] : mimeTypes;
			this._reader = reader;
			this._writer = writer;
			this.readerSpi = readerSpi;
			this.writerSpi = writerSpi;
		}

		protected static void newInstance(Map<String, Codec> codecs, String[] formatNames, String[] suffixes, String[] mimeTypes, Class<? extends ImageReader> reader,
				Class<? extends ImageWriter> writer) {
			Codec codec = new Codec(formatNames, suffixes, mimeTypes, reader, writer, null, null);
			for (String fn: formatNames) {
				codecs.put(fn.toUpperCase(), codec);
			}
		}

		protected static void newInstanceSpi(Map<String, Codec> codecs, String[] formatNames, String[] suffixes, String[] mimeTypes, Class<? extends ImageReaderSpi> readerSpi,
				Class<? extends ImageWriterSpi> writerSpi) {
			Codec codec = new Codec(formatNames, suffixes, mimeTypes, null, null, readerSpi, writerSpi);
			for (String fn: formatNames) {
				codecs.put(fn.toUpperCase(), codec);
			}
		}

		protected static void newInstance(Map<String, Codec> codecs, String[] formatNames, String[] suffixes, String[] mimeTypes, String reader,
				Class<? extends ImageWriter> writer) {

			Class<? extends ImageReader> r = null;
			try {
				r = loadReader(AImageIOInterface.class.getClassLoader(), reader);
			}
			catch (IOException e) {
				Config config = CFMLEngineFactory.getInstance().getThreadConfig();
				if (config != null) {
					Log log = config.getLog("application");
					if (log != null) log.error("image-extension", e);
				}
			}
			newInstance(codecs, formatNames, suffixes, mimeTypes, r, writer);
		}

		protected static void newInstance(Map<String, Codec> codecs, String[] formatNames, String[] suffixes, String[] mimeTypes, Class<? extends ImageReader> reader,
				String writer) {

			Class<? extends ImageWriter> w = null;
			try {
				w = loadWriter(AImageIOInterface.class.getClassLoader(), writer);
			}
			catch (IOException e) {
				Config config = CFMLEngineFactory.getInstance().getThreadConfig();
				if (config != null) {
					Log log = config.getLog("application");
					if (log != null) log.error("image-extension", e);
				}
			}
			newInstance(codecs, formatNames, suffixes, mimeTypes, reader, w);
		}

		protected static void newInstance(Map<String, Codec> codecs, String[] formatNames, String[] suffixes, String[] mimeTypes, String reader, String writer) {

			Class<? extends ImageReader> r = null;
			try {
				r = loadReader(AImageIOInterface.class.getClassLoader(), reader);
			}
			catch (IOException e) {
				Config config = CFMLEngineFactory.getInstance().getThreadConfig();
				if (config != null) {
					Log log = config.getLog("application");
					if (log != null) log.error("image-extension", e);
				}
			}
			Class<? extends ImageWriter> w = null;
			try {
				w = loadWriter(AImageIOInterface.class.getClassLoader(), writer);
			}
			catch (IOException e) {
				Config config = CFMLEngineFactory.getInstance().getThreadConfig();
				if (config != null) {
					Log log = config.getLog("application");
					if (log != null) log.error("image-extension", e);
				}
			}
			newInstance(codecs, formatNames, suffixes, mimeTypes, r, w);
		}

		public boolean hasReader() {
			return _reader != null || readerSpi != null;
		}

		public boolean hasWriter() {
			return _writer != null || writerSpi != null;
		}
	}

	private static class ImageReaderSpiImpl extends ImageReaderSpi {

		private Class<? extends ImageReader> clazz;
		private Codec codec;
		private Class<? extends ImageInputStream> imageInputStreamClazz;

		public ImageReaderSpiImpl(Class<? extends ImageReader> clazz, Codec codec) {
			this.clazz = clazz;
			this.codec = codec;
		}

		public void setType(Class<? extends ImageInputStream> imageInputStreamClazz) {
			this.imageInputStreamClazz = imageInputStreamClazz;

		}

		@Override
		public boolean canDecodeInput(Object source) throws IOException {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ImageReader createReaderInstance(Object extension) throws IOException {
			try {
				boolean addThis = true;
				Constructor<? extends ImageReader> constr;
				try {
					constr = clazz.getDeclaredConstructor(READ_CONSTR1);

				}
				catch (NoSuchMethodException nsme) {
					constr = clazz.getDeclaredConstructor(READ_CONSTR0);
					addThis = false;
				}
				constr.setAccessible(true);
				if (addThis) return constr.newInstance(this);
				return constr.newInstance();
			}
			catch (Exception e) {
				print.e(e);

				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
		}

		@Override
		public String getDescription(Locale locale) {
			return "Lucee impl to handle OSGi enviroment";
		}

		@Override
		public Class<?>[] getInputTypes() {
			return new Class[] { imageInputStreamClazz };
		}

		@Override
		public ImageReader createReaderInstance() throws IOException {
			return createReaderInstance(null);
		}

		@Override
		public boolean isOwnReader(ImageReader reader) {
			return reader != null && reader.getClass().getName().equals(clazz.getName());
		}

		@Override
		public String[] getImageWriterSpiNames() {
			// TODO Auto-generated method stub
			return super.getImageWriterSpiNames();
		}

		@Override
		public String[] getFormatNames() {
			return codec.formatNames;
		}

		@Override
		public String[] getFileSuffixes() {
			return codec.suffixes;
		}

		@Override
		public String[] getMIMETypes() {
			return codec.mimeTypes;
		}

		@Override
		public String getPluginClassName() {
			// TODO Auto-generated method stub
			return super.getPluginClassName();
		}

		@Override
		public boolean isStandardStreamMetadataFormatSupported() {
			// TODO Auto-generated method stub
			return super.isStandardStreamMetadataFormatSupported();
		}

		@Override
		public String getNativeStreamMetadataFormatName() {
			// TODO Auto-generated method stub
			return super.getNativeStreamMetadataFormatName();
		}

		@Override
		public String[] getExtraStreamMetadataFormatNames() {
			// TODO Auto-generated method stub
			return super.getExtraStreamMetadataFormatNames();
		}

		@Override
		public boolean isStandardImageMetadataFormatSupported() {
			// TODO Auto-generated method stub
			return super.isStandardImageMetadataFormatSupported();
		}

		@Override
		public String getNativeImageMetadataFormatName() {
			// TODO Auto-generated method stub
			return super.getNativeImageMetadataFormatName();
		}

		@Override
		public String[] getExtraImageMetadataFormatNames() {
			// TODO Auto-generated method stub
			return super.getExtraImageMetadataFormatNames();
		}

		@Override
		public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
			// TODO Auto-generated method stub
			return super.getStreamMetadataFormat(formatName);
		}

		@Override
		public IIOMetadataFormat getImageMetadataFormat(String formatName) {
			// TODO Auto-generated method stub
			return super.getImageMetadataFormat(formatName);
		}

		@Override
		public void onRegistration(ServiceRegistry registry, Class<?> category) {
			// TODO Auto-generated method stub
			super.onRegistration(registry, category);
		}

		@Override
		public void onDeregistration(ServiceRegistry registry, Class<?> category) {
			// TODO Auto-generated method stub
			super.onDeregistration(registry, category);
		}

		@Override
		public String getVendorName() {
			// TODO Auto-generated method stub
			return super.getVendorName();
		}

		@Override
		public String getVersion() {
			// TODO Auto-generated method stub
			return super.getVersion();
		}

	}

	private static class ImageWriterSpiImpl extends ImageWriterSpi {

		private Class<? extends ImageWriter> clazz;
		private Class<? extends ImageOutputStream> imageOutputStreamClazz;
		private Codec codec;

		public ImageWriterSpiImpl(Class<? extends ImageWriter> clazz, Codec codec) {
			this.clazz = clazz;
			this.codec = codec;
		}

		public void setType(Class<? extends ImageOutputStream> imageOutputStreamClazz) {
			this.imageOutputStreamClazz = imageOutputStreamClazz;

		}

		@Override
		public boolean canEncodeImage(ImageTypeSpecifier type) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public ImageWriter createWriterInstance(Object extension) throws IOException {
			boolean addThis = true;
			try {
				Constructor<? extends ImageWriter> constr;
				try {
					constr = clazz.getDeclaredConstructor(WRITE_CONSTR1);

				}
				catch (NoSuchMethodException nsme) {
					constr = clazz.getDeclaredConstructor(WRITE_CONSTR0);
					addThis = false;
				}

				constr.setAccessible(true);
				if (addThis) return constr.newInstance(this);
				return constr.newInstance();
			}
			catch (Exception e) {
				print.e(e);
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
		}

		@Override
		public String getDescription(Locale locale) {
			return "Lucee impl to handle OSGi enviroment";
		}

		@Override
		public boolean isFormatLossless() {
			// TODO Auto-generated method stub
			return super.isFormatLossless();
		}

		@Override
		public Class<?>[] getOutputTypes() {
			return new Class[] { this.imageOutputStreamClazz };
		}

		@Override
		public boolean canEncodeImage(RenderedImage im) {
			// TODO Auto-generated method stub
			return super.canEncodeImage(im);
		}

		@Override
		public ImageWriter createWriterInstance() throws IOException {
			// TODO Auto-generated method stub
			return super.createWriterInstance();
		}

		@Override
		public boolean isOwnWriter(ImageWriter writer) {
			// TODO Auto-generated method stub
			return super.isOwnWriter(writer);
		}

		@Override
		public String[] getImageReaderSpiNames() {
			// TODO Auto-generated method stub
			return super.getImageReaderSpiNames();
		}

		@Override
		public String[] getFormatNames() {
			return codec.formatNames;
		}

		@Override
		public String[] getFileSuffixes() {
			return codec.suffixes;
		}

		@Override
		public String[] getMIMETypes() {
			return codec.mimeTypes;
		}

		@Override
		public String getPluginClassName() {
			// TODO Auto-generated method stub
			return super.getPluginClassName();
		}

		@Override
		public boolean isStandardStreamMetadataFormatSupported() {
			// TODO Auto-generated method stub
			return super.isStandardStreamMetadataFormatSupported();
		}

		@Override
		public String getNativeStreamMetadataFormatName() {
			// TODO Auto-generated method stub
			return super.getNativeStreamMetadataFormatName();
		}

		@Override
		public String[] getExtraStreamMetadataFormatNames() {
			// TODO Auto-generated method stub
			return super.getExtraStreamMetadataFormatNames();
		}

		@Override
		public boolean isStandardImageMetadataFormatSupported() {
			// TODO Auto-generated method stub
			return super.isStandardImageMetadataFormatSupported();
		}

		@Override
		public String getNativeImageMetadataFormatName() {
			// TODO Auto-generated method stub
			return super.getNativeImageMetadataFormatName();
		}

		@Override
		public String[] getExtraImageMetadataFormatNames() {
			// TODO Auto-generated method stub
			return super.getExtraImageMetadataFormatNames();
		}

		@Override
		public IIOMetadataFormat getStreamMetadataFormat(String formatName) {
			// TODO Auto-generated method stub
			return super.getStreamMetadataFormat(formatName);
		}

		@Override
		public IIOMetadataFormat getImageMetadataFormat(String formatName) {
			// TODO Auto-generated method stub
			return super.getImageMetadataFormat(formatName);
		}

		@Override
		public void onRegistration(ServiceRegistry registry, Class<?> category) {
			// super.onRegistration(registry, category);
		}

		@Override
		public void onDeregistration(ServiceRegistry registry, Class<?> category) {
			// super.onDeregistration(registry, category);
		}

		@Override
		public String getVendorName() {
			return "Lucee";
		}

		@Override
		public String getVersion() {
			return "0";
		}

	}
}
