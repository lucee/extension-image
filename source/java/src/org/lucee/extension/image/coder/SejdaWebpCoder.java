package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import com.luciad.imageio.webp.WebPImageReaderSpi;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.util.Util;

public class SejdaWebpCoder extends Coder {

	private String[] writerFormatNames = new String[] { "WEBP" };
	private String[] readerFormatNames = new String[] { "WEBP" };

	@Override
	public BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException {
		if (!Util.isEmpty(format, true) && !format.trim().equalsIgnoreCase("webp"))
			throw new IOException("format [" + format + "] not supported, the only format supported by this coder is [webp]");

		if (res instanceof File) return createBufferedImage(res);

		InputStream is = null;
		try {
			is = res.getInputStream();
			return createBufferedImage(is);
		}
		finally {
			Util.closeEL(is);
		}
	}

	@Override
	public BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		if (!Util.isEmpty(format, true) && !format.trim().equalsIgnoreCase("webp"))
			throw new IOException("format [" + format + "] not supported, the only format supported by this coder is [webp]");
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(bytes);
			return createBufferedImage(bais);
		}
		finally {
			Util.closeEL(bais);
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

	private BufferedImage createBufferedImage(Object input) throws IOException {
		javax.imageio.stream.ImageInputStream stream = ImageIO.createImageInputStream(input);
		BufferedImage bufferedImage;
		ImageReader reader = null;
		try {
			reader = new WebPImageReaderSpi().createReaderInstance();
			ImageReadParam param = reader.getDefaultReadParam();
			reader.setInput(stream, true, true);
			bufferedImage = reader.read(0, param);
		}
		finally {
			if (reader != null) reader.dispose();
			if (stream != null) {
				try {
					stream.close();
				}
				catch (Exception e) {
				}
			}
		}
		return bufferedImage;
	}

}
