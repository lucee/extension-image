package org.lucee.extension.image.coder;

/**
 * Probes whether NightMonkeys native libraries (libheif, libjxl) are loadable.
 * Used to avoid registering ImageIO SPIs when natives are absent — otherwise
 * {@code HeifImageReaderSpi.onRegistration} poisons {@code ImageIO} init on Java 22+.
 */
final class NightMonkeysNative {

	private static Boolean available;

	private NightMonkeysNative() {
	}

	static boolean isAvailable() {
		if (available != null)
			return available;
		if (Runtime.version().feature() < 22)
			return available = Boolean.FALSE;
		available = canLoadLibrary("heif") && canLoadLibrary("jxl");
		return available;
	}

	private static boolean canLoadLibrary(String name) {
		try {
			System.loadLibrary(name);
			return true;
		}
		catch (UnsatisfiedLinkError e) {
			return false;
		}
	}

}
