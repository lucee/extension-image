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
import lucee.runtime.ext.function.Function;

public class ImageWrite extends FunctionSupport implements Function {

	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc, name, null, 0.75, true, false);
	}

	public static String call(PageContext pc, Object name, String destination) throws PageException {
		return call(pc, name, destination, 0.75, true, false);
	}

	public static String call(PageContext pc, Object name, String destination, double quality) throws PageException {
		return call(pc, name, destination, quality, true, false);
	}

	public static String call(PageContext pc, Object name, String destination, double quality, boolean overwrite) throws PageException {
		return call(pc, name, destination, quality, overwrite, false);
	}

	public static String call(PageContext pc, Object name, String destination, double quality, boolean overwrite, boolean noMeta) throws PageException {
		// if(name instanceof String)name=pc.getVariable(Caster.toString(name));
		Image image = Image.toImage(pc, name);
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if (quality < 0 || quality > 1)
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "ImageWrite", 3, "quality", "value have to be between 0 and 1", null);

		Resource res = eng.getStringUtil().isEmpty(destination) ? image.getSource() : eng.getResourceUtil().toResourceNotExisting(pc, destination);

		// MUST beide boolschen argumente checken
		if (res == null) return null;
		try {
			image.writeOut(res, overwrite, (float) quality, noMeta);
		}
		catch (IOException e) {
			throw eng.getCastUtil().toPageException(e);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 5) return call(pc, args[0], cast.toString(args[1]), cast.toDoubleValue(args[2]), cast.toBooleanValue(args[3]), cast.toBooleanValue(args[4]));
		if (args.length == 4) return call(pc, args[0], cast.toString(args[1]), cast.toDoubleValue(args[2]), cast.toBooleanValue(args[3]), false);
		if (args.length == 3) return call(pc, args[0], cast.toString(args[1]), cast.toDoubleValue(args[2]), true, false);
		if (args.length == 2) return call(pc, args[0], cast.toString(args[1]), 0.75, true, false);
		if (args.length == 1) return call(pc, args[0], null, 0.75, true, false);
		throw exp.createFunctionException(pc, "ImageWrite", 1, 5, args.length);
	}
}