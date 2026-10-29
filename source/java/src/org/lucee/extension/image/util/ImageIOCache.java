package org.lucee.extension.image.util;

import javax.imageio.ImageIO;

/**
 * Controls the JVM wide ImageIO disk cache (LDEV-3327).
 * 
 * By default the ImageIO temp file cache is switched off, so reading and writing images uses memory
 * instead of creating temp files. Because {@link ImageIO#setUseCache(boolean)} is a JVM wide
 * setting, it is applied once, from the static initializer of this class. Every class that calls
 * ImageIO calls {@link #init()} from its own static initializer, so the setting is in place before
 * the first ImageIO call, no matter which entry point is used first.
 * 
 * To re-enable the cache set the environment variable {@code LUCEE_IMAGE_USE_CACHE} or the system
 * property {@code lucee.image.use.cache} to {@code true}. The system property wins over the
 * environment variable.
 */
public final class ImageIOCache {

	public static final String SYSPROP_NAME = "lucee.image.use.cache";
	public static final String ENVVAR_NAME = "LUCEE_IMAGE_USE_CACHE";

	private static final boolean USE_CACHE;

	static {
		USE_CACHE = readUseCache();
		ImageIO.setUseCache(USE_CACHE);
	}

	private ImageIOCache() {
	}

	/**
	 * Does nothing by itself, calling it makes sure this class is initialized and the ImageIO cache
	 * setting is applied.
	 */
	public static void init() {
		// the work is done in the static initializer
	}

	/**
	 * @return the value that was applied to ImageIO.setUseCache
	 */
	public static boolean isUseCache() {
		return USE_CACHE;
	}

	private static boolean readUseCache() {
		String value = null;
		try {
			value = System.getProperty(SYSPROP_NAME);
			if (value == null || value.trim().isEmpty()) value = System.getenv(ENVVAR_NAME);
		}
		catch (SecurityException se) {
			// not allowed to read it, use the default
		}
		return value != null && "true".equalsIgnoreCase(value.trim());
	}
}
