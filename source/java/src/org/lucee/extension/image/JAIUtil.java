/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package org.lucee.extension.image;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import org.lucee.extension.image.util.IDGenerator;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;

public class JAIUtil {

	private static Method getAsBufferedImage;
	private static Method create1;
	private static Method create3;

	public static boolean isJAISupported() {
		return true;
	}

	public static BufferedImage read(Resource res) throws IOException {
		Resource tmp = null;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			tmp = eng.getSystemUtil().getTempDirectory().getRealResource(IDGenerator.intId() + "-" + res.getName());
			eng.getIOUtil().copy(res, tmp);
			// Object im = JAI.create("fileload", res.getAbsolutePath());
			return getAsBufferedImage(create("fileload", tmp.getAbsolutePath()));
		}
		finally {
			if (tmp != null) tmp.delete();
		}
	}

	public static BufferedImage read(InputStream is, String format) throws IOException {
		if ("jpg".equalsIgnoreCase(format)) format = "JPEG";

		Resource tmp = null;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			tmp = eng.getSystemUtil().getTempDirectory().getRealResource(IDGenerator.intId() + (eng.getStringUtil().isEmpty(format) ? "" : "." + format));
			eng.getIOUtil().copy(is, tmp, false);
			// Object im = JAI.create("fileload", tmp.getAbsolutePath());
			return getAsBufferedImage(create("fileload", tmp.getAbsolutePath()));
		}
		finally {
			if (tmp != null) tmp.delete();
		}
	}

	public static void write(BufferedImage img, Resource res, String format) throws IOException {
		Resource tmp = res;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			tmp = eng.getSystemUtil().getTempDirectory().getRealResource(IDGenerator.intId() + "-" + res.getName());

			// JAI.create("filestore", img, tmp.getAbsolutePath(),format);
			create("filestore", img, tmp.getAbsolutePath(), format);
			eng.getIOUtil().copy(tmp, res);

		}
		finally {
			if (tmp != null) tmp.delete();
		}
	}

	public static void write(BufferedImage img, OutputStream os, String format, boolean closeStream) throws IOException {
		Resource tmp = null;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			tmp = eng.getSystemUtil().getTempDirectory().getRealResource(IDGenerator.intId() + "." + format);
			create("filestore", img, tmp.getAbsolutePath(), format);
			eng.getIOUtil().copy(tmp.getInputStream(), os, true, closeStream);
		}
		finally {
			if (tmp != null) tmp.delete();
		}
	}

	////////////////////////////////////////////////////////////////////

	private static Object create(String name, Object param) throws IOException {
		try {
			return create1().invoke(null, new Object[] { name, param });
		}
		catch (Exception e) {
			throw toIOException(e);
		}
	}

	private static Object create(String name, Object img, Object param1, Object param2) throws IOException {
		try {
			return create3().invoke(null, new Object[] { name, img, param1, param2 });
		}
		catch (Exception e) {
			throw toIOException(e);
		}
	}

	private synchronized static BufferedImage getAsBufferedImage(Object im) throws IOException {
		// RenderedOp.getAsBufferedImage();
		PrintStream err = System.err;
		try {
			System.setErr(new PrintStream(DevNullOutputStream.DEV_NULL_OUTPUT_STREAM));
			return (BufferedImage) getAsBufferedImage().invoke(im, new Object[0]);
		}
		catch (Exception e) {
			throw toIOException(e);
		}
		finally {
			System.setErr(err);
		}
	}

	private static Method getAsBufferedImage() throws IOException {
		if (getAsBufferedImage == null) {
			try {
				getAsBufferedImage = getRenderedOp().getMethod("getAsBufferedImage", new Class[0]);
			}
			catch (Exception e) {
				throw toIOException(e);
			}
		}
		return getAsBufferedImage;
	}

	private static Method create1() throws IOException {
		if (create1 == null) {
			try {
				create1 = getJAI().getMethod("create", new Class[] { String.class, Object.class });
			}
			catch (Exception e) {
				throw toIOException(e);
			}
		}
		return create1;
	}

	private static Method create3() throws IOException {
		if (create3 == null) {
			try {
				create3 = getJAI().getMethod("create", new Class[] { String.class, RenderedImage.class, Object.class, Object.class });
			}
			catch (Exception e) {
				throw toIOException(e);
			}
		}
		return create3;
	}

	private static Class getRenderedOp() throws IOException {
		return RenderedOp.class;
	}

	private static Class getJAI() throws IOException {
		return JAI.class;
	}

	private static IOException toIOException(Throwable e) {
		if (e instanceof InvocationTargetException) e = ((InvocationTargetException) e).getTargetException();

		if (e instanceof IOException) return (IOException) e;
		IOException ioe = new IOException(e.getMessage());
		ioe.setStackTrace(e.getStackTrace());
		return ioe;
	}

	/**
	 * dev null output stream, write data to nirvana
	 */
	private static class DevNullOutputStream extends OutputStream implements Serializable {

		private static final long serialVersionUID = 3170712166551346336L;
		public static final DevNullOutputStream DEV_NULL_OUTPUT_STREAM = new DevNullOutputStream();
		public static final PrintStream DEV_NULL_PRINT_STREAM = new PrintStream(new DevNullOutputStream());

		/**
		 * Constructor of the class
		 */
		private DevNullOutputStream() {
		}

		@Override
		public void close() {
		}

		@Override
		public void flush() {
		}

		@Override
		public void write(byte[] b, int off, int len) {
		}

		@Override
		public void write(byte[] b) {
		}

		@Override
		public void write(int b) {
		}

	}
}