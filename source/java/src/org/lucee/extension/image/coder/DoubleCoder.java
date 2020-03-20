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

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;

class DoubleCoder extends Coder {

	private Coder first;
	private Coder second;

	private String[] writerFormatNames;
	private String[] readerFormatNames;

	public DoubleCoder(Coder first, Coder second) {
		this.first = first;
		this.second = second;
	}

	@Override
	public BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException {
		try {
			return first.toBufferedImage(res, format, jpegColorType);
		}
		catch (Exception ioe) {
			return second.toBufferedImage(res, format, jpegColorType);
		}
	}

	@Override
	public BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException {
		try {
			return first.toBufferedImage(bytes, format, jpegColorType);
		}
		catch (Exception ioe) {
			return second.toBufferedImage(bytes, format, jpegColorType);
		}
	}

	@Override
	public final String[] getWriterFormatNames() {
		if (writerFormatNames == null) {
			writerFormatNames = JRECoder.mixTogetherOrdered(first.getWriterFormatNames(), second.getWriterFormatNames());
		}
		return writerFormatNames;
	}

	@Override
	public final String[] getReaderFormatNames() {
		if (readerFormatNames == null) {
			readerFormatNames = JRECoder.mixTogetherOrdered(first.getReaderFormatNames(), second.getReaderFormatNames());
		}
		return readerFormatNames;
	}

}