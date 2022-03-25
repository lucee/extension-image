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
import java.util.ArrayList;
import java.util.List;

import org.lucee.extension.image.Image;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.config.Config;

public abstract class Coder {

	private static Coder instance;

	protected Coder() {
	}

	public static Coder getInstance() {

		if (instance == null) {
			List<Coder> listCoders = new ArrayList<>();
			Config config = CFMLEngineFactory.getInstance().getThreadConfig();
			Log log = config == null ? null : config.getLog("application");

			// JDeli
			try {
				JDeliCoder jdeliCoder = new JDeliCoder();
				if (jdeliCoder.supported()) {
					listCoders.add(jdeliCoder);
					if (log != null) log.info("image", "use JDeli Image Decoder");
					// System.out.println("use JDeli Image Coder ");
				}
			}
			catch (Exception e) {
				if (log != null) log.error("image", e);
			}

			// JRE
			JRECoder jreCoder = new JRECoder();
			listCoders.add(jreCoder);
			if (log != null) log.info("image", "use JRE Image En/Decoder");

			// Sanselan/Commons Imaging - used last because Sanselan has troubles with JPG (inverted colors)
			try {
				SanselanCoder sanselanCoder = new SanselanCoder();
				if (sanselanCoder.supported()) {
					listCoders.add(sanselanCoder);
					if (log != null) log.info("image", "use Sanselan Image En/Decoder");
					// System.out.println("use Sanselan Image Coder ");
				}
			}
			catch (Exception e) {
				if (log != null) log.error("image", e);
			}
			if (listCoders.size() < 2) instance = jreCoder;
			else instance = new MultiCoder(listCoders.toArray(new Coder[listCoders.size()]));
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
	public abstract BufferedImage read(Resource res, String format, RefInteger jpegColorType) throws IOException;

	/**
	 * translate a binary array to a buffered image
	 * 
	 * @param binary
	 * @return
	 * @throws IOException
	 */
	public abstract BufferedImage read(byte[] bytes, String format, RefInteger jpegColorType) throws IOException;

	public abstract String[] getWriterFormatNames() throws IOException;

	public abstract String[] getReaderFormatNames() throws IOException;

	public abstract boolean supported();

	public abstract String getFormat(Resource res) throws IOException;

	public abstract String getFormat(Resource res, String defaultValue);

	public abstract String getFormat(byte[] bytes) throws IOException;

	public abstract String getFormat(byte[] bytes, String defaultValue);

	public abstract void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException;

	public abstract void write(Image img, OutputStream os, String format, float quality, boolean closeStream, boolean noMeta) throws IOException;

}