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
package org.lucee.extension.image.functions;

import java.io.IOException;

import org.lucee.extension.image.Image;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class ImageWriteBase64 extends FunctionSupport {

	public static String call(PageContext pc, Object name, String destination, String format) throws PageException {
		return call(pc, name, destination, format, false);
	}

	public static String call(PageContext pc, Object name, String destination, String format, boolean inHTMLFormat) throws PageException {
		// if(name instanceof String)name=pc.getVariable(Caster.toString(name));
		Image image = Image.toImage(pc, name);
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Resource res = eng.getStringUtil().isEmpty(destination) ? image.getSource() : eng.getResourceUtil().toResourceNotExisting(pc, destination);

		try {
			return image.writeBase64(res, format, inHTMLFormat);
		}
		catch (IOException e) {
			throw eng.getCastUtil().toPageException(e);
		}

	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, args[0], cast.toString(args[1]), cast.toString(args[2]), cast.toBooleanValue(args[3]));
		if (args.length == 3) return call(pc, args[0], cast.toString(args[1]), cast.toString(args[2]));
		throw exp.createFunctionException(pc, "ImageWriteBase64", 3, 4, args.length);
	}
}