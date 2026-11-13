package org.lucee.extension.image.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import javax.imageio.spi.IIORegistry;

import org.lucee.extension.image.coder.Coder;
import org.lucee.extension.image.coder.NightMonkeysNative;

import lucee.commons.io.log.Log;

/**
 * Safe replacement for {@link javax.imageio.ImageIO#scanForPlugins()} (LDEV-6592).
 * 
 * {@code ImageIO.scanForPlugins()} registers every ImageIO plugin it finds through
 * {@code META-INF/services}, and aborts the whole scan on the first error. The NightMonkeys
 * HEIF/JXL readers load libheif/libjxl in {@code onRegistration}. On Java 22+ without those native
 * libraries the first registration fails quietly, but it leaves the native binding class in a failed
 * state, so every later registration throws {@code NoClassDefFoundError: Could not initialize class
 * com.github.gotson.nightmonkeys.heif.Heif}. That error escaped from the static initializer of
 * {@code Image} and broke every image function, not only HEIF.
 * 
 * This scan does the same work, but
 * <ul>
 * <li>skips the NightMonkeys providers (checked by class name, before they are instantiated) when
 * the native libraries are not available; {@code NightMonkeysCoder} registers them itself when they
 * are,</li>
 * <li>registers each provider on its own, so one broken plugin is logged and skipped instead of
 * stopping the scan or failing the caller,</li>
 * <li>removes NightMonkeys providers that are still registered without their native libraries. The
 * JDK catches the error from {@code onRegistration}, but the provider then stays registered, and
 * its {@code canDecodeInput} throws the same {@code NoClassDefFoundError} from every
 * {@code ImageIO.read}, whatever the format.</li>
 * </ul>
 */
public final class ImageIOPlugins {

	private static final String NIGHTMONKEYS_PACKAGE = "com.github.gotson.nightmonkeys.";

	private ImageIOPlugins() {
	}

	/**
	 * Scans the context class loader of the current thread, like {@code ImageIO.scanForPlugins()}.
	 * Never throws.
	 * 
	 * @return number of providers registered
	 */
	public static int scanForPlugins() {
		ClassLoader cl = null;
		try {
			cl = Thread.currentThread().getContextClassLoader();
		}
		catch (SecurityException se) {
			// fall back to our own class loader
		}
		if (cl == null) cl = ImageIOPlugins.class.getClassLoader();
		return scanForPlugins(cl);
	}

	/**
	 * Registers the ImageIO plugins the given class loader provides. Never throws.
	 * 
	 * @param cl class loader to scan
	 * @return number of providers registered
	 */
	public static int scanForPlugins(ClassLoader cl) {
		int count = 0;
		IIORegistry registry;
		try {
			registry = IIORegistry.getDefaultInstance();
		}
		catch (Throwable t) {
			rethrowIfFatal(t);
			log("could not get the ImageIO registry", t);
			return 0;
		}

		boolean nightMonkeys = nightMonkeysAvailable();
		Iterator<Class<?>> categories = registry.getCategories();
		while (categories.hasNext()) {
			Class<?> category = categories.next();
			Iterator<? extends ServiceLoader.Provider<?>> providers;
			try {
				providers = ServiceLoader.load(category, cl).stream().iterator();
			}
			catch (Throwable t) {
				rethrowIfFatal(t);
				log("could not list the ImageIO plugins for [" + category.getName() + "]", t);
				continue;
			}
			while (true) {
				ServiceLoader.Provider<?> provider;
				try {
					if (!providers.hasNext()) break;
					provider = providers.next();
				}
				catch (Throwable t) {
					// the stream cannot continue after a broken services entry, keep what we have
					rethrowIfFatal(t);
					log("could not list the ImageIO plugins for [" + category.getName() + "]", t);
					break;
				}
				count += register(registry, provider, nightMonkeys) ? 1 : 0;
			}
		}
		if (!nightMonkeys) removeNightMonkeys(registry);
		return count;
	}

	/**
	 * Deregisters every NightMonkeys provider from the given registry, for example one a previous
	 * {@code ImageIO.scanForPlugins()} left behind.
	 * 
	 * @return number of providers removed
	 */
	static int removeNightMonkeys(IIORegistry registry) {
		int count = 0;
		try {
			Iterator<Class<?>> categories = registry.getCategories();
			while (categories.hasNext()) {
				Class<?> category = categories.next();
				List<Object> remove = new ArrayList<>();
				Iterator<?> it = registry.getServiceProviders(category, false);
				while (it.hasNext()) {
					Object provider = it.next();
					if (provider != null && provider.getClass().getName().startsWith(NIGHTMONKEYS_PACKAGE)) remove.add(provider);
				}
				for (Object provider: remove) {
					if (registry.deregisterServiceProvider(provider, (Class) category)) count++;
				}
			}
		}
		catch (Throwable t) {
			rethrowIfFatal(t);
			log("could not remove the NightMonkeys ImageIO plugins", t);
		}
		return count;
	}

	private static boolean register(IIORegistry registry, ServiceLoader.Provider<?> provider, boolean nightMonkeys) {
		String name = "?";
		try {
			Class<?> type = provider.type();
			name = type.getName();
			if (!nightMonkeys && name.startsWith(NIGHTMONKEYS_PACKAGE)) return false;
			registry.registerServiceProvider(provider.get());
			return true;
		}
		catch (Throwable t) {
			rethrowIfFatal(t);
			log("could not register the ImageIO plugin [" + name + "], it is skipped", t);
			return false;
		}
	}

	private static boolean nightMonkeysAvailable() {
		try {
			return NightMonkeysNative.isAvailable();
		}
		catch (Throwable t) {
			rethrowIfFatal(t);
			return false;
		}
	}

	private static void rethrowIfFatal(Throwable t) {
		if (t instanceof VirtualMachineError) throw (VirtualMachineError) t;
	}

	private static void log(String msg, Throwable t) {
		try {
			Log log = Coder.log();
			if (log != null) log.log(Log.LEVEL_WARN, "image", msg, t);
		}
		catch (Throwable ignore) {
			rethrowIfFatal(ignore);
		}
	}
}
