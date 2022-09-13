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
			Config config = CFMLEngineFactory.getInstance().getThreadConfig();
			Log log = config == null ? null : config.getLog("application");

			MultiCoder mc = new MultiCoder();

			add(mc, "org.lucee.extension.image.coder.JDeliCoder", log);
			add(mc, "org.lucee.extension.image.coder.AsposeCoder", log);
			add(mc, "org.lucee.extension.image.coder.TwelveMonkeysCoder", log);
			add(mc, "org.lucee.extension.image.coder.ImageIOCoder", log);
			add(mc, "org.lucee.extension.image.coder.LuceeCoder", log);
			add(mc, "org.lucee.extension.image.coder.ApacheImagingCoder", log);
			add(mc, "org.lucee.extension.image.coder.JAICoder", log);

			instance = mc;
		}
		return instance;
	}

	private static void add(MultiCoder mc, String className, Log log) {
		try {
			Coder coder = (Coder) mc.getClass().getClassLoader().loadClass(className).newInstance();

			if (coder.supported()) {
				mc.add(coder);
				if (log != null) log.info("image", "use JDeli Image En/Decoder");
			}
		}
		catch (Exception e) {
			if (log != null) log.error("image", e);
			// else e.printStackTrace();
		}
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

	public abstract boolean supported();

	public abstract void write(Image img, Resource destination, String format, float quality, boolean noMeta) throws IOException;

	public static Log log() {
		// FUTURE PageContext pc = CFMLEngineFactory.getInstance().getThreadPageContext();
		// FUTURE if(pc!=null)pc.getLog("application");
		Config config = CFMLEngineFactory.getInstance().getThreadConfig();
		if (config != null) return config.getLog("application");
		return null;
	}

	// public abstract void write(Image img, OutputStream os, String format, float quality, boolean
	// closeStream, boolean noMeta) throws IOException;

}