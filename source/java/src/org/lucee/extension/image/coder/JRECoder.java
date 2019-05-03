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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.JAIUtil;
import org.lucee.extension.image.PSDReader;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

class JRECoder extends Coder {

	private String[] writerFormatNames;
	private String[] readerFormatNames;

	protected JRECoder() {
		super();
	}

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(Resource res, String format) throws IOException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(res);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			InputStream is = null;
			try {
				reader.read(is = res.getInputStream());
				return reader.getImage();
			}
			finally {
				Util.closeEL(is);
			}
		}

		InputStream is = null;
		try {
			return ImageIO.read(is = res.getInputStream());
		}
		catch (Exception e) {}
		finally {
			Util.closeEL(is);
		}

		return JAIUtil.read(res);
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(byte[] bytes, String format) throws IOException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(bytes, null);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			reader.read(new ByteArrayInputStream(bytes));
			return reader.getImage();
		}

		try {
			return ImageIO.read(new ByteArrayInputStream(bytes));
		}
		catch (Exception e) {}

		return JAIUtil.read(new ByteArrayInputStream(bytes), format);
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			String[] iio = ImageIO.getWriterFormatNames();
			String[] jai = null;// JAIUtil.isJAISupported()?JAIUtil.getSupportedWriteFormat():null;
			writerFormatNames = mixTogetherOrdered(iio, jai);
		}
		return writerFormatNames;
	}

	@Override
	public final String[] getReaderFormatNames() {
		if (readerFormatNames == null) {
			String[] iio = ImageIO.getReaderFormatNames();
			String[] jai = null;// JAIUtil.isJAISupported()?JAIUtil.getSupportedReadFormat():null;
			readerFormatNames = mixTogetherOrdered(iio, jai);
		}
		return readerFormatNames;
	}

	public static final String[] mixTogetherOrdered(String[] names1, String[] names2) {
		Set<String> set = new HashSet<String>();

		if (names1 != null) for (int i = 0; i < names1.length; i++) {
			set.add(names1[i].toLowerCase());
		}
		if (names2 != null) for (int i = 0; i < names2.length; i++) {
			set.add(names2[i].toLowerCase());
		}

		names1 = set.toArray(new String[set.size()]);
		Arrays.sort(names1);
		return names1;
	}
}