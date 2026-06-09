package org.lucee.extension.image.coder;

import com.github.gotson.nightmonkeys.heif.imageio.plugins.HeifImageReaderSpi;
import com.github.gotson.nightmonkeys.jxl.imageio.plugins.JxlImageReaderSpi;

public class NightMonkeysCoder extends AImageIOInterface {

	public NightMonkeysCoder() {
		Codec.newInstanceSpi(codecs, new String[] { "HEIF", "HEIC", "AVIF" }, new String[] { "heif", "heic", "avif" },
				new String[] { "image/heif", "image/heic", "image/avif" }, HeifImageReaderSpi.class, null);
		Codec.newInstanceSpi(codecs, new String[] { "JXL", "JPEGXL" }, new String[] { "jxl" }, new String[] { "image/jxl" }, JxlImageReaderSpi.class, null);
		init();
	}

	@Override
	public boolean supported() {
		return isJava22OrLater() && super.supported();
	}

	private static boolean isJava22OrLater() {
		return Runtime.version().feature() >= 22;
	}

}
