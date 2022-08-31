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
import java.util.List;

import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;

public abstract class Coder {

	private static Coder instance;

	protected Coder() {
	}

	public static Coder getInstance() {

		if (instance == null) {
			instance = new JRECoder();
			List<Coder> coders = new ArrayList<>();
			coders.add(instance);

			// Sanselan
			try {
				SanselanCoder coder = new SanselanCoder();
				coders.add(coder); // used JRE first because Sanselan has troubles with JPG (inverted colors)
				// SystemOut.printDate("use JRE and Sanselan Image Coder ");
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			// Seida
			try {
				SejdaWebpCoder coder = new SejdaWebpCoder();
				coders.add(coder);
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			if (coders.size() > 1) {
				instance = new MultiCoder(coders);
			}
		}

		return instance;
	}

	/**
	 * translate a file resource to a buffered image
	 * 
	 * @param res
	 * @return
	 * @throws IOException
	 */
	public abstract BufferedImage toBufferedImage(Resource res, String format, RefInteger jpegColorType) throws IOException;

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	public abstract BufferedImage toBufferedImage(byte[] bytes, String format, RefInteger jpegColorType) throws IOException;

	public abstract String[] getWriterFormatNames();

	public abstract String[] getReaderFormatNames();

}