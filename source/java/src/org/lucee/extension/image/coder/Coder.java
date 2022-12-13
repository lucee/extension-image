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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.util.CommonUtil;

import lucee.commons.io.log.Log;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.types.RefInteger;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.type.Array;

public abstract class Coder {

	private static Coder instance;

	protected Coder() {
	}

	public static Coder getInstance(PageContext pc) {

		if (true || instance == null) {
			Config config = CFMLEngineFactory.getInstance().getThreadConfig();
			Log log = config == null ? null : config.getLog("application");

			MultiCoder mc = new MultiCoder();
			Set<String> coders = CommonUtil.getCoders(pc);
			if (coderAllowed(coders, "JDeli")) add(mc, "org.lucee.extension.image.coder.JDeliCoder", log);
			if (coderAllowed(coders, "Gotson")) add(mc, "org.lucee.extension.image.coder.GotsonCoder", log);
			if (coderAllowed(coders, "Aspose")) add(mc, "org.lucee.extension.image.coder.AsposeCoder", log);
			if (coderAllowed(coders, "TwelveMonkeys")) add(mc, "org.lucee.extension.image.coder.TwelveMonkeysCoder", log);
			if (coderAllowed(coders, "ImageIO")) add(mc, "org.lucee.extension.image.coder.ImageIOCoder", log);
			if (coderAllowed(coders, "Lucee")) add(mc, "org.lucee.extension.image.coder.LuceeCoder", log);
			if (coderAllowed(coders, "ApacheImaging")) add(mc, "org.lucee.extension.image.coder.ApacheImagingCoder", log);
			if (coderAllowed(coders, "JAI")) add(mc, "org.lucee.extension.image.coder.JAICoder", log);

			instance = mc;
		}
		return instance;
	}

	private static boolean coderAllowed(Set<String> coders, String name) {
		if (coders == null) return true;
		return coders.contains(name.toLowerCase());
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
	public static String[] sortAndMerge(String[] names) {
		List<String> list = new ArrayList<>();
		for (String n: names) {
			n = n.toUpperCase();
			if ("JPG".equals(n)) n = "JPEG";
			if ("JPE".equals(n)) n = "JPEG";
			if (!list.contains(n)) list.add(n.toUpperCase());
		}
		String[] arr = list.toArray(new String[list.size()]);
		Arrays.sort(arr);
		return arr;
	}

	public static Array sortAndConvert(List<String> list) {
		Collections.sort(list);

		Array arr = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		for (String str: list) {
			arr.appendEL(str);
		}
		return arr;
	}

	public static Array sortAndConvert(String[] strArr) {
		Arrays.sort(strArr);

		Array arr = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		for (String str: strArr) {
			arr.appendEL(str);
		}
		return arr;
	}
}