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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.Img;
import org.lucee.extension.image.JAIUtil;
import org.lucee.extension.image.PSDReader;
import org.lucee.extension.image.jpg.JpegReader;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
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
	public final BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(res);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			InputStream is = null;
			try {
				reader.read(is = res.getInputStream());
				BufferedImage bi = reader.getImage();
				if (bi != null) return bi;
			}
			finally {
				Util.closeEL(is);
			}
		}
		else if ("jpg".equalsIgnoreCase(format)) {
			JpegReader reader = new JpegReader();
			try {
				if (res instanceof File) {
					BufferedImage bi = reader.readImage((File) res, jpegColorType);
					if (bi != null) return bi;
				}
				else {
					Resource tmp = eng.getSystemUtil().getTempFile("jpg", false);
					eng.getIOUtil().copy(res, tmp);
					try {
						BufferedImage bi = reader.readImage((File) tmp, jpegColorType);
						if (bi != null) return bi;
					}
					finally {
						if (!tmp.delete()) ((File) tmp).deleteOnExit();
					}
				}

			}
			catch (Exception e) {
			}
		}

		InputStream is = null;
		try {
			BufferedImage bi = ImageIO.read(is = res.getInputStream());
			if (bi != null) return bi;
		}
		catch (Exception e) {
		}
		finally {
			Util.closeEL(is);
		}

		try {
			BufferedImage bi = JAIUtil.read(is = res.getInputStream(), format); // TODO use JAIUtil.read(Resource); with   the locking issue on that method
			if (bi != null) return bi;
		}
		catch (Exception e) {
			if (res instanceof File) {
				Img img = new Img((File) res);
				BufferedImage bi = img.getBufferedImage();
				if (bi != null) return bi;
			}
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		finally { // this finally block should be removed when the reverting back to use JAIUtil.read(Resource);
			Util.closeEL(is);
		}
		return null;
	}

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	@Override
	public final BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (eng.getStringUtil().isEmpty(format)) format = ImageUtil.getFormat(bytes, null);
		if ("psd".equalsIgnoreCase(format)) {
			PSDReader reader = new PSDReader();
			reader.read(new ByteArrayInputStream(bytes));
			BufferedImage bi = reader.getImage();
			if (bi != null) return bi;
		}
		else if ("jpg".equalsIgnoreCase(format)) {
			JpegReader reader = new JpegReader();
			try {
				BufferedImage bi = reader.readImage(bytes, jpegColorType);
				if (bi != null) return bi;
			}
			catch (Exception e) {
				throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
			}
		}

		try {
			BufferedImage bi = ImageIO.read(new ByteArrayInputStream(bytes));
			if (bi != null) return bi;
		}
		catch (Exception e) {
		}

		try {
			BufferedImage bi = JAIUtil.read(new ByteArrayInputStream(bytes), format.equalsIgnoreCase("jpg") ? "JPEG" : format);
			if (bi != null) return bi;
		}
		catch (Exception e) {
			Img img = new Img(bytes);
			BufferedImage bi = img.getBufferedImage();
			if (bi != null) return bi;
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		return null;
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			String[] iio = ImageIO.getWriterFormatNames();
			String[] jai = null;
			writerFormatNames = mixTogetherOrdered(iio, jai);
		}
		return writerFormatNames;
	}

	@Override
	public final String[] getReaderFormatNames() {
		if (readerFormatNames == null) {
			String[] iio = ImageIO.getReaderFormatNames();
			String[] jai = null;
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