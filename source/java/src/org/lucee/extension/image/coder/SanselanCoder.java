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

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;

class SanselanCoder extends Coder {

	private String[] writerFormatNames = new String[] { "PNG", "GIF", "TIFF", "JPEG", "BMP", "PNM", "PGM", "PBM", "PPM", "XMP" };
	private String[] readerFormatNames = new String[] { "PNG", "GIF", "TIFF", "JPEG", "BMP", "PNM", "PGM", "PBM", "PPM", "XMP", "ICO", "PSD" };

	protected SanselanCoder() {
		super();
		Imaging.hasImageFileExtension("lucee.gif");// to make sure Sanselan exist when load this class
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
		InputStream is = null;
		try {
			return Imaging.getBufferedImage(is = res.getInputStream());
		}
		catch (ImageReadException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
		finally {
			Util.closeEL(is);
		}
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
		try {
			return Imaging.getBufferedImage(new ByteArrayInputStream(bytes));
		}
		catch (ImageReadException e) {
			throw CFMLEngineFactory.getInstance().getExceptionUtil().toIOException(e);
		}
	}

	@Override
	public String[] getWriterFormatNames() {
		return writerFormatNames;
	}

	@Override
	public String[] getReaderFormatNames() {
		return readerFormatNames;
	}
}