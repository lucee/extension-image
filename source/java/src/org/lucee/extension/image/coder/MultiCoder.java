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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.lucee.extension.image.ImageUtil;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.util.Util;

class MultiCoder extends Coder {

	private String[] writerFormatNames;
	private String[] readerFormatNames;

	private Coder[] coders;

	public MultiCoder(List<Coder> coders) {
		this.coders = coders.toArray(new Coder[0]);
	}

	@Override
	public BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException {
		IOException ioe = null;
		if (Util.isEmpty(format, true)) {
			format = ImageUtil.getFormat(res);
		}

		for (Coder c: coders) {
			try {
				if (!Util.isEmpty(format, true) && !formatSupported(c, format)) {
					continue;
				}

				BufferedImage bi = c.toBufferedImage(res, format, jpegColorType);
				if (bi != null) return bi;
			}
			catch (IOException e) {
				ioe = e;
			}
		}
		if (ioe != null) throw ioe;
		return null;
	}

	@Override
	public BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		IOException ioe = null;

		if (Util.isEmpty(format, true)) {
			format = ImageUtil.getFormat(bytes);
		}

		for (Coder c: coders) {
			try {
				if (!Util.isEmpty(format, true) && !formatSupported(c, format)) {
					continue;
				}
				BufferedImage bi = c.toBufferedImage(bytes, format, jpegColorType);
				if (bi != null) return bi;
			}
			catch (IOException e) {
				e.printStackTrace();
				ioe = e;
			}
		}
		if (ioe != null) throw ioe;
		return null;
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			List<String[]> formats = new ArrayList<>();
			for (Coder c: coders) {
				formats.add(c.getWriterFormatNames());
			}
			writerFormatNames = mixTogetherOrdered(formats);
		}
		return writerFormatNames;
	}

	@Override
	public final String[] getReaderFormatNames() {
		if (readerFormatNames == null) {
			List<String[]> formats = new ArrayList<>();
			for (Coder c: coders) {
				formats.add(c.getReaderFormatNames());
			}
			readerFormatNames = mixTogetherOrdered(formats);
		}
		return readerFormatNames;
	}

	private static final String[] mixTogetherOrdered(List<String[]> listNames) {
		Set<String> set = new HashSet<String>();

		Iterator<String[]> it = listNames.iterator();
		while (it.hasNext()) {
			for (String name: it.next()) {
				set.add(name.toLowerCase());
			}
		}
		String[] names = set.toArray(new String[set.size()]);
		Arrays.sort(names);
		return names;
	}

	private boolean formatSupported(Coder c, String format) {
		String[] formats = c.getReaderFormatNames();
		if (formats == null) return true;
		for (String f: formats) {
			if (format.equalsIgnoreCase(f)) return true;
		}
		return false;
	}

}