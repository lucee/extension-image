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
package org.lucee.extension.image.coder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.format.FormatExtract;
import org.lucee.extension.image.format.FormatNames;
import org.lucee.extension.image.util.MultiException;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.exp.CatchBlock;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

public class MultiCoder extends Coder implements FormatNames, FormatExtract {

	private List<Coder> coders = new ArrayList();

	private String[] writerFormatNames;
	private String[] readerFormatNames;
	private static final String tokenw = "MultiCoderTokenWrite";
	private static final String tokenr = "MultiCoderTokenRead";

	public MultiCoder() {
	}

	public void add(Coder coder) {
		writerFormatNames = null;
		readerFormatNames = null;
		coders.add(coder);
	}

	@Override
	public BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		return read(res, format, jpegColorType, null);
	}

	public BufferedImage read(Resource res, String format, RefInteger jpegColorType, Array detail) throws IOException {
		if (Util.isEmpty(format, true)) {
			format = ImageUtil.getFormat(res);
		}

		MultiException me = null;
		BufferedImage bi = null;
		Struct data = null;
		long start = 0;
		for (Coder coder: coders) {
			if (coder instanceof FormatNames && !_supported(((FormatNames) coder).getReaderFormatNames(), format)) continue;
			try {

				if (detail != null) {
					Creation cre = CFMLEngineFactory.getInstance().getCreationUtil();
					data = cre.createStruct();
					detail.appendEL(data);
					data.set("class", coder.getClass().getName());
					start = System.currentTimeMillis();
				}
				bi = coder.read(res, format, jpegColorType);
				if (detail != null) {
					data.set("time", System.currentTimeMillis() - start);
					data.set("image", new Image(bi));
				}
				if (detail == null && bi != null) return bi;
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
				if (detail != null) {
					data.setEL("exception", toCatchBlock(t));
				}
				if (me == null) me = new MultiException(t);
				else me.initCause(t);
			}
		}
		if (bi != null) return bi;
		if (me != null && detail == null) throw toIOException(me);
		return null;
	}

	private CatchBlock toCatchBlock(Throwable t) throws IOException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		try {
			Class<?> clazz = eng.getClassUtil().loadClass("lucee.runtime.exp.CatchBlockImpl");
			Constructor<?> constr = clazz.getConstructor(PageException.class);
			return (CatchBlock) constr.newInstance(eng.getCastUtil().toPageException(t));
		}
		catch (Exception e1) {
			throw eng.getExceptionUtil().toIOException(e1);
		}
	}

	@Override
	public BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		if (Util.isEmpty(format, true)) {
			format = ImageUtil.getFormat(bytes);
		}

		MultiException me = null;
		for (Coder coder: coders) {
			if (coder instanceof FormatNames && !_supported(((FormatNames) coder).getReaderFormatNames(), format)) continue;
			try {
				long start = System.currentTimeMillis();
				BufferedImage bi = coder.read(bytes, format, jpegColorType);
				if (bi != null) return bi;
			}
			catch (Exception e) {
				if (me == null) me = new MultiException(e);
				else me.initCause(e);
			}
		}
		if (me != null) {
			throw toIOException(me);
		}
		return null;

	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		write(img, destination, format, quality, noMeta, null);
	}

	public void write(Image img, Resource destination, String format, final float quality, boolean noMeta, Array detail) throws IOException {
		if (Util.isEmpty(format, true)) {
			format = img.getFormat();
		}
		Struct data = null;
		long start = 0;
		Resource tmp = null;
		MultiException me = null;
		for (Coder coder: coders) {
			if (coder instanceof FormatNames && !_supported(((FormatNames) coder).getWriterFormatNames(), format)) continue;
			try {
				if (detail != null) {
					Creation cre = CFMLEngineFactory.getInstance().getCreationUtil();
					data = cre.createStruct();
					detail.appendEL(data);
					data.set("class", coder.getClass().getName());
					start = System.currentTimeMillis();
					destination = tmp = ImageUtil.createTempFile(format);
				}
				coder.write(img, destination, format, quality, noMeta);
				if (detail != null) {
					data.set("time", System.currentTimeMillis() - start);
				}
				if (detail == null) return;
			}
			catch (Throwable t) {
				if (t instanceof ThreadDeath) throw (ThreadDeath) t;
				if (detail != null) {
					data.setEL("exception", toCatchBlock(t));
				}
				if (me == null) me = new MultiException(t);
				else me.initCause(t);
			}
			finally {
				if (tmp != null && !tmp.delete() && tmp instanceof File) ((File) tmp).deleteOnExit();
				tmp = null;
			}
		}
		if (me != null && detail == null) throw toIOException(me);

	}

	@Override
	public String getFormat(Resource res) throws IOException {
		MultiException me = null;
		for (Coder coder: coders) {
			if (!(coder instanceof FormatExtract)) continue;
			try {
				String format = ((FormatExtract) coder).getFormat(res);
				if (!Util.isEmpty(format)) return format;
			}
			catch (Exception e) {
				if (me == null) me = new MultiException(e);
				else me.initCause(e);
			}
		}
		if (me != null) throw toIOException(me);
		return null;
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		MultiException me = null;
		for (Coder coder: coders) {
			if (!(coder instanceof FormatExtract)) continue;
			try {
				String format = ((FormatExtract) coder).getFormat(bytes);
				if (!Util.isEmpty(format)) return format;
			}
			catch (Exception e) {
				if (me == null) me = new MultiException(e);
				else me.initCause(e);
			}
		}
		if (me != null) throw toIOException(me);
		return null;
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		for (Coder coder: coders) {
			if (!(coder instanceof FormatExtract)) continue;
			String format = ((FormatExtract) coder).getFormat(res, null);
			if (!Util.isEmpty(format)) return format;
		}
		return defaultValue;
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		for (Coder coder: coders) {
			if (!(coder instanceof FormatExtract)) continue;
			String format = ((FormatExtract) coder).getFormat(bytes, null);
			if (!Util.isEmpty(format)) return format;
		}
		return defaultValue;
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			synchronized (tokenw) {
				if (writerFormatNames == null) {
					List<String> list = new ArrayList<>();
					for (Coder c: coders) {
						if (!(c instanceof FormatNames)) continue;
						try {
							for (String n: ((FormatNames) c).getWriterFormatNames()) {
								if (!list.contains(n.toUpperCase())) list.add(n.toUpperCase());
							}
						}
						catch (Exception e) {
						}
					}
					writerFormatNames = list.toArray(new String[list.size()]);
					Arrays.sort(writerFormatNames);
				}
			}
		}
		return writerFormatNames;
	}

	@Override
	public final String[] getReaderFormatNames() {
		if (readerFormatNames == null) {
			synchronized (tokenr) {
				if (readerFormatNames == null) {
					List<String> list = new ArrayList<>();
					for (Coder c: coders) {
						if (!(c instanceof FormatNames)) continue;
						try {
							for (String n: ((FormatNames) c).getReaderFormatNames()) {
								if (!list.contains(n.toUpperCase())) list.add(n.toUpperCase());
							}
						}
						catch (Exception e) {
						}
					}
					readerFormatNames = list.toArray(new String[list.size()]);
					Arrays.sort(readerFormatNames);
				}
			}
		}
		return readerFormatNames;
	}

	public final Struct getWriterFormatNamesByGroup() {
		Creation cre = CFMLEngineFactory.getInstance().getCreationUtil();
		Struct sct = cre.createStruct();
		for (Coder c: coders) {
			if (!(c instanceof FormatNames)) continue;
			try {
				List<String> list = new ArrayList<>();
				for (String n: ((FormatNames) c).getWriterFormatNames()) {
					if (!list.contains(n.toUpperCase())) list.add(n.toUpperCase());
				}
				sct.setEL(c.getClass().getName(), sortAndConvert(list));
			}
			catch (Exception e) {
			}
		}
		return sct;
	}

	public final Struct getReaderFormatNamesByGroup() {
		Creation cre = CFMLEngineFactory.getInstance().getCreationUtil();
		Struct sct = cre.createStruct();
		for (Coder c: coders) {
			if (!(c instanceof FormatNames)) continue;
			try {
				List<String> list = new ArrayList<>();
				// filter out duplicates
				for (String n: ((FormatNames) c).getReaderFormatNames()) {
					if (!list.contains(n.toUpperCase())) list.add(n.toUpperCase());
				}
				sct.setEL(c.getClass().getName(), sortAndConvert(list));

			}
			catch (Exception e) {
			}

		}
		return sct;
	}

	@Override
	public boolean supported() {
		return true;
	}

	public boolean _supported(String[] formats, String format) {
		if (formats == null || format == null) return true;
		for (String f: formats) {
			if (format.equalsIgnoreCase(f)) return true;
		}
		return false;
	}

	private static IOException toIOException(MultiException me) {
		if (me.size() == 1) return CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(me.getThrowable(0));
		else return CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(me);
	}
}