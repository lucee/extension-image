package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import openize.heic.decoder.HeicImage;
import openize.heic.decoder.PixelFormat;
import openize.io.IOFileStream;
import openize.io.IOMode;

class OpenizeHeicCoder extends Coder implements FormatNames, FormatExtract {

	private String[] readerFormatNames = sortAndMerge(new String[] { "HEIC", "HEIF" });
	private String[] writerFormatNames = new String[0];

	@Override
	public BufferedImage read(Resource res, String format) throws IOException {
		if (res instanceof File) {
			return read((File) res);
		}
		Resource tmp = CFMLEngineFactory.getInstance().getSystemUtil().getTempFile("heic", false);
		try {
			CFMLEngineFactory.getInstance().getIOUtil().copy(res, tmp);
			return read((File) tmp);
		}
		finally {
			if (!tmp.delete()) ((File) tmp).deleteOnExit();
		}
	}

	@Override
	public BufferedImage read(byte[] bytes, String format) throws IOException {
		Resource tmp = CFMLEngineFactory.getInstance().getSystemUtil().getTempFile("heic", false);
		OutputStream os = null;
		try {
			os = tmp.getOutputStream();
			os.write(bytes);
			return read((File) tmp);
		}
		finally {
			Util.closeEL(os);
			if (!tmp.delete()) ((File) tmp).deleteOnExit();
		}
	}

	private BufferedImage read(File file) throws IOException {
		try (IOFileStream fs = new IOFileStream(file, IOMode.READ)) {
			HeicImage image = HeicImage.load(fs);
			int width = (int) image.getWidth();
			int height = (int) image.getHeight();
			if (width <= 0 || height <= 0) {
				throw new IOException("invalid HEIC image dimensions [" + width + "x" + height + "]");
			}
			BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			int[] pixels = image.getInt32Array(PixelFormat.Argb32);
			if (pixels == null) {
				throw new IOException("HEIC image contains no pixel data");
			}
			bi.setRGB(0, 0, width, height, pixels, 0, width);
			return bi;
		}
		catch (openize.io.IOException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		throw new IOException("OpenizeHeicCoder does not support writing");
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		if (res.length() == 0) {
			String ext = CFMLEngineFactory.getInstance().getResourceUtil().getExtension(res, null);
			if ("heif".equalsIgnoreCase(ext)) return "heif";
			if ("heic".equalsIgnoreCase(ext)) return "heic";
			throw new IOException("cannot detect HEIC format from an empty file");
		}
		return getFormatFromStream(res);
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length == 0) throw new IOException("cannot detect HEIC format from empty bytes");
		Resource tmp = CFMLEngineFactory.getInstance().getSystemUtil().getTempFile("heic", false);
		OutputStream os = null;
		try {
			os = tmp.getOutputStream();
			os.write(bytes);
			return getFormatFromStream(tmp);
		}
		finally {
			Util.closeEL(os);
			if (!tmp.delete()) ((File) tmp).deleteOnExit();
		}
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		try {
			return getFormat(res);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		try {
			return getFormat(bytes);
		}
		catch (Exception e) {
			return defaultValue;
		}
	}

	private String getFormatFromStream(Resource res) throws IOException {
		if (res instanceof File) {
			return getFormatFromFile((File) res);
		}
		Resource tmp = CFMLEngineFactory.getInstance().getSystemUtil().getTempFile("heic", false);
		try {
			CFMLEngineFactory.getInstance().getIOUtil().copy(res, tmp);
			return getFormatFromFile((File) tmp);
		}
		finally {
			if (!tmp.delete()) ((File) tmp).deleteOnExit();
		}
	}

	private String getFormatFromFile(File file) throws IOException {
		try (IOFileStream fs = new IOFileStream(file, IOMode.READ)) {
			if (!HeicImage.canLoad(fs)) {
				throw new IOException("not a HEIC/HEIF image");
			}
		}
		catch (openize.io.IOException e) {
			throw new IOException(e.getMessage(), e);
		}
		String ext = null;
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		if (dot >= 0 && dot < name.length() - 1) ext = name.substring(dot + 1);
		if ("heif".equalsIgnoreCase(ext)) return "heif";
		return "heic";
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
		return !isJava22OrLater();
	}

	private static boolean isJava22OrLater() {
		return Runtime.version().feature() >= 22;
	}

}
