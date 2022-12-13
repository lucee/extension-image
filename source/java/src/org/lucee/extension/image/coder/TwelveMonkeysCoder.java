package org.lucee.extension.image.coder;

import java.io.IOException;

import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.lucee.extension.image.util.print;

import com.twelvemonkeys.imageio.plugins.bmp.BMPImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.bmp.BMPImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageReader;
import com.twelvemonkeys.imageio.plugins.bmp.ICOImageWriter;
import com.twelvemonkeys.imageio.plugins.icns.ICNSImageReader;
import com.twelvemonkeys.imageio.plugins.icns.ICNSImageWriter;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriter;
import com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.psd.PSDImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageReaderSpi;
import com.twelvemonkeys.imageio.plugins.tiff.TIFFImageWriterSpi;
import com.twelvemonkeys.imageio.plugins.webp.WebPImageReaderSpi;

public class TwelveMonkeysCoder extends AImageIOInterface {

	public TwelveMonkeysCoder() {

		try {
			print.e(new WebPImageReaderSpi().createReaderInstance());
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Codec.newInstanceSpi(codecs, new String[] { "bmp", "BMP" }, new String[] { "bmp", "rle" }, new String[] { "image/bmp", "image/x-bmp", "image/vnd.microsoft.bitmap" },
				BMPImageReaderSpi.class, BMPImageWriterSpi.class);
		// Codec.newInstance(codecs, new String[] { "bmp", "BMP" }, new String[] { "bmp", "rle" }, new
		// String[] { "image/bmp", "image/x-bmp", "image/vnd.microsoft.bitmap" },
		// BMPImageReader.class, BMPImageWriter.class);

		Codec.newInstance(codecs, new String[] { "icns", "ICNS" }, new String[] { "icns" }, new String[] { "image/x-apple-icons" }, ICNSImageReader.class, ICNSImageWriter.class);

		Codec.newInstance(codecs, new String[] { "ico", "ICO" }, new String[] { "ico" }, new String[] { "image/vnd.microsoft.icon", "image/x-icon", "image/ico" },
				ICOImageReader.class, ICOImageWriter.class);

		Codec.newInstance(codecs, new String[] { "JPEG", "jpeg" }, new String[] { "jpeg", "jpg", "jpe" }, new String[] { "image/jpeg" }, JPEGImageReader.class,
				JPEGImageWriter.class, null, new Class[] { JPEGImageWriterSpi.class, ImageWriter.class });

		Codec.newInstanceSpi(codecs, new String[] { "psd", "PSD", "psb", "PSB" }, new String[] { "psd", "psb" },
				new String[] { "image/vnd.adobe.photoshop", "application/vnd.adobe.photoshop", "image/x-psd", "application/x-photoshop", "image/x-photoshop" },
				PSDImageReaderSpi.class, PSDImageWriterSpi.class);
		// Codec.newInstance(codecs, new String[] { "psd", "PSD", "psb", "PSB" }, new String[] { "psd",
		// "psb" },
		// new String[] { "image/vnd.adobe.photoshop", "application/vnd.adobe.photoshop", "image/x-psd",
		// "application/x-photoshop", "image/x-photoshop" },
		// PSDImageReader.class, PSDImageWriter.class);

		Codec.newInstanceSpi(codecs, new String[] { "tiff", "TIFF", "tif", "TIF" }, new String[] { "tif", "tiff" }, new String[] { "image/tiff", "image/x-tiff" },
				TIFFImageReaderSpi.class, TIFFImageWriterSpi.class);

		// Codec.newInstance(codecs, new String[] { "tiff", "TIFF", "tif", "TIF" }, new String[] { "tif",
		// "tiff" }, new String[] { "image/tiff", "image/x-tiff" },
		// TIFFImageReader.class, TIFFImageWriter.class);

		Codec.newInstanceSpi(codecs, new String[] { "webp", "WEBP", "wbp", "WBP" }, new String[] { "wbp", "webp" }, new String[] { "image/webp", "image/x-webp" },
				WebPImageReaderSpi.class, (Class<? extends ImageWriterSpi>) null);

		// Codec.newInstance(codecs, new String[] { "webp", "WEBP", "wbp", "WBP" }, new String[] { "wbp",
		// "webp" }, new String[] { "image/webp", "image/x-webp" },
		// "com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", (Class<? extends ImageWriter>) null);

		/*
		 * 
		 * 
		 * Codec.newInstance(codecs, new String[] { "tiff", "TIFF", "tif", "TIF" }, new String[] { "tif",
		 * "tiff" }, new String[] { "image/tiff", "image/x-tiff" }, TIFFImageReader.class,
		 * TIFFImageWriter.class); Codec.newInstance(codecs, new String[] { "webp", "WEBP", "wbp", "WBP" },
		 * new String[] { "wbp", "webp" }, new String[] { "image/webp", "image/x-webp" },
		 * "com.twelvemonkeys.imageio.plugins.webp.WebPImageReader", (Class<? extends ImageWriter>) null);
		 */
		init();
	}

}
