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
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.lucee.extension.image.Image;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

class MultiCoder extends Coder {

	private Coder[] coders;

	private String[] writerFormatNames;
	private String[] readerFormatNames;
	private static final String tokenw = "MultiCoderTokenWrite";
	private static final String tokenr = "MultiCoderTokenRead";

	public MultiCoder(Coder[] coders) {
		this.coders = coders;
	}

	@Override
	public BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				BufferedImage bi = coder.read(res, format, jpegColorType);
				if (bi != null) return bi;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
		return null;
	}

	@Override
	public BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				BufferedImage bi = coder.read(bytes, format, jpegColorType);
				if (bi != null) return bi;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
		return null;

	}

	@Override
	public void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				coder.write(img, destination, format, quality, noMeta);
				return;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
	}

	@Override
	public void write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				coder.write(img, os, format, quality, closeStream, noMeta);
				return;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
	}

	@Override
	public String getFormat(Resource res) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				String format = coder.getFormat(res);
				if (!Util.isEmpty(format)) return format;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
		return null;
	}

	@Override
	public String getFormat(byte[] bytes) throws IOException {
		Exception exception = null;
		for (Coder coder: coders) {
			try {
				String format = coder.getFormat(bytes);
				if (!Util.isEmpty(format)) return format;
			}
			catch (Exception e) {
				exception = e;
			}
		}
		if (exception != null) throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(exception);
		return null;
	}

	@Override
	public String getFormat(Resource res, String defaultValue) {
		for (Coder coder: coders) {
			String format = coder.getFormat(res, null);
			if (!Util.isEmpty(format)) return format;
		}
		return defaultValue;
	}

	@Override
	public String getFormat(byte[] bytes, String defaultValue) {
		for (Coder coder: coders) {
			String format = coder.getFormat(bytes, null);
			if (!Util.isEmpty(format)) return format;
		}
		return defaultValue;
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			synchronized (tokenw) {
				if (writerFormatNames == null) {
					Set<String> set = new HashSet<>();
					for (Coder c: coders) {
						try {
							for (String n: c.getWriterFormatNames()) {
								set.add(n);
							}
						}
						catch (Exception e) {
						}
					}
					writerFormatNames = set.toArray(new String[set.size()]);
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
					Set<String> set = new HashSet<>();
					for (Coder c: coders) {
						try {
							for (String n: c.getReaderFormatNames()) {
								set.add(n);
							}
						}
						catch (Exception e) {
						}
					}
					readerFormatNames = set.toArray(new String[set.size()]);
					Arrays.sort(readerFormatNames);
				}
			}
		}
		return readerFormatNames;
	}

	@Override
	public boolean supported() {
		return true;
	}

}