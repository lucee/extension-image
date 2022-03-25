package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

import org.lucee.extension.image.Image;

import com.twelvemonkeys.imageio.plugins.bmp.BMPImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.BMPImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.bmp.CURImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.icns.ICNSImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.icns.ICNSImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.PageException;

class ImageIOCoder extends Coder {

	private static final String token = "ImageIOCoderToken";
	private static boolean isInit;

	protected ImageIOCoder() {
		super();
		if (!isInit) {
			synchronized (token) {
				// TODO instead of init flag, check if it exist
				if (!isInit) {
					if (supported()) {
						IIORegistry registry = IIORegistry.getDefaultInstance();
						registry.registerServiceProvider(new WebPImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new BMPImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new CURImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new ICNSImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new ICOImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new PSDImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new WebPImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new JPEGImageReaderSpi(), ImageReaderSpi.class);
						registry.registerServiceProvider(new TIFFImageReaderSpi(), ImageReaderSpi.class);

						registry.registerServiceProvider(new BMPImageWriterSpi(), ImageWriterSpi.class);
						registry.registerServiceProvider(new ICOImageWriterSpi(), ImageWriterSpi.class);
						registry.registerServiceProvider(new ICNSImageWriterSpi(), ImageWriterSpi.class);
						registry.registerServiceProvider(new JPEGImageWriterSpi(), ImageWriterSpi.class);
						registry.registerServiceProvider(new PSDImageWriterSpi(), ImageWriterSpi.class);
						registry.registerServiceProvider(new TIFFImageWriterSpi(), ImageWriterSpi.class);

						// registry.registerServiceProviders(ServiceRegistry.lookupProviders(ImageWriterSpi.class));
						System.out.println("Registered image readers: " + Arrays.asList(ImageIO.getReaderMIMETypes()));
						System.out.println("Registered image writers: " + Arrays.asList(ImageIO.getWriterMIMETypes()));
					}
					isInit = true;
				}
			}
		}
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
		write(img, destination.getOutputStream(), format, quality, noMeta, noMeta);
	}

	@Override
	public void write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		if (Util.isEmpty(format)) format = img.getFormat();

		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(os);
			ImageIO.write(img.getBufferedImage(), format, ios);
		}
		catch (PageException pe) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(pe);
		}
		finally {
			os.flush();
			if (closeStream) {
				ios.close();
				Util.closeEL(os);
			}
		}

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
		return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null));
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(bytes, null));
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		return getFormatbyMimeType(CFMLEngineFactory.getInstance().getResourceUtil().getMimeType(res, null), defaultValue);
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