package org.lucee.extension.image.coder;

import java.io.IOException;

import com.luciad.imageio.webp.WebPImageReaderSpi;
import com.luciad.imageio.webp.WebPImageWriterSpi;

public class GotsonCoder extends AImageIOInterface {

	public GotsonCoder() {
		Codec.newInstanceSpi(codecs, new String[] { "webp", "WEBP", "wbp", "WBP" }, new String[] { "wbp", "webp" }, new String[] { "image/webp", "image/x-webp" },
				WebPImageReaderSpi.class, WebPImageWriterSpi.class);
		init();
	}

	public static void main(String[] args) throws IOException {
		new WebPImageReaderSpi().createReaderInstance();
	}

}
