package org.lucee.extension.image.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiException extends IOException {
	private static final long serialVersionUID = 5995189415710848117L;

	private LinkedList<Throwable> nested = new LinkedList<Throwable>();

	public MultiException(Throwable t) {
		super("Multiple exceptions");
		if (t != null) initCause(t);
	}

	@Override
	public Throwable initCause(Throwable t) {
		Throwable rtn;
		if (nested.isEmpty()) {
			rtn = super.initCause(t);
		}
		else {
			Throwable last = nested.getLast();

			Throwable c;
			while (last != (c = last.getCause())) {
				if (c == null) break;
				last = c;

			}
			rtn = last.initCause(t);
		}

		if (t instanceof MultiException) {
			for (Throwable tt: ((MultiException) t).nested)
				nested.add(tt);
		}
		else nested.add(t);
		return rtn;
	}

	/* ------------------------------------------------------------ */
	public int size() {
		return nested.size();
	}

	/* ------------------------------------------------------------ */
	public List<Throwable> getThrowables() {

		List<Throwable> copy = new ArrayList<>();
		for (Throwable t: nested) {
			copy.add(t);
		}

		return copy;
	}

	/* ------------------------------------------------------------ */
	public Throwable getThrowable(int i) {
		return nested.get(i);
	}

	/* ------------------------------------------------------------ */
	@Override
	public String toString() {
		if (nested.size() > 0) return MultiException.class.getSimpleName() + nested;
		return MultiException.class.getSimpleName() + "[]";
	}

	@Override
	public String getMessage() {
		StringBuilder sb = new StringBuilder("Multiple exceptions (");
		boolean first = true;
		for (Throwable t: nested) {
			if (!first) sb.append("; ");
			sb.append(t.getMessage());
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	public static void main(String[] args) {
		all();
	}

	public static void all() {
		List<Exception> list = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			list.add(one(i));
		}
		mergeExceptions(list).printStackTrace();
		// print.e(mergeExceptions(list).getMessage());
	}

	public static Exception one(int index) {
		try {
			/*
			 * if (index < -3) throw new IOException("shit happens: a" + index); if (index < -6) throw new
			 * IOException("shit happens: b" + index); else {
			 */
			Exception ex;
			try {
				throw new IOException("shit happens: x" + index);

			}
			catch (Exception ee) {
				ex = ee;
			}

			IOException ioe = new IOException("shit happens: c" + index);
			ioe.initCause(ex);
			throw ioe;
			// }
		}
		catch (Exception e) {
			return e;
		}
	}

	public static Exception mergeExceptions(List<? extends Throwable> ts) {
		MultiException me = new MultiException(null);
		for (Throwable t: ts) {
			me.initCause(t);
		}

		return me;
	}
}
