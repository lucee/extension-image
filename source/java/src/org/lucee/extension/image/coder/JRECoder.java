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
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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

		try {
			return JAIUtil.read(res);
		}
		catch (Exception e) {
			if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
				InputStream _is = null;
				try {
					return cmyk2rgb(_is = res.getInputStream());
				}
				catch (Exception ee) {
					ee.printStackTrace();
				}
				finally {
					Util.closeEL(_is);
				}
			}
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}

	}

	public BufferedImage cmyk2rgb(Object obj) throws IOException {

		// Find a suitable ImageReader
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
		ImageReader reader = null;
		while (readers.hasNext()) {
			reader = readers.next();
			if (reader.canReadRaster()) {
				break;
			}
		}

		// Stream the image file (the original CMYK image)
		ImageInputStream input = ImageIO.createImageInputStream(obj);
		reader.setInput(input);

		// Read the image raster
		Raster raster = reader.readRaster(0, null);

		// Create a new RGB image
		BufferedImage bi = new BufferedImage(raster.getWidth(), raster.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

		// Fill the new image with the old raster
		bi.getRaster().setRect(raster);
		return bi;
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

		try {
			return JAIUtil.read(new ByteArrayInputStream(bytes), format);
		}
		catch (Exception e) {
			if ("jpg".equalsIgnoreCase(format) || "jpeg".equalsIgnoreCase(format)) {
				try {
					return cmyk2rgb(new ByteArrayInputStream(bytes));
				}
				catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
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