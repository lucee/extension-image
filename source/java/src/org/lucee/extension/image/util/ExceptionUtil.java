package org.lucee.extension.image.util;

public class ExceptionUtil {
	public static void initCauseEL(Throwable e, Throwable cause) {
		if (cause == null || e == cause)
			return;

		// get current root cause
		Throwable tmp;
		int count = 100;
		do {
			if (--count <= 0)
				break; // in case cause point to a child
			tmp = e.getCause();
			if (tmp == null)
				break;
			if (tmp == cause)
				return;
			e = tmp;
		} while (true);

		if (e == cause)
			return;
		// attach to root cause
		try {
			e.initCause(cause);
		} catch (Exception ex) {
		}
	}
}
