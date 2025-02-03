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

import java.awt.RenderingHints;

import org.lucee.extension.image.Image;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class ImageShear extends FunctionSupport {
	public static String call(PageContext pc, Object name, double shear) throws PageException {
		return call(pc, name, shear, "horizontal", "nearest");
	}

	public static String call(PageContext pc, Object name, double shear, String direction) throws PageException {
		return call(pc, name, shear, direction, "nearest");
	}

	public static String call(PageContext pc, Object name, double shear, String strDirection, String strInterpolation) throws PageException {
		// if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc, name);

		// direction
		strDirection = strDirection.toLowerCase().trim();
		int direction;
		if ("horizontal".equals(strDirection)) direction = Image.SHEAR_HORIZONTAL;
		else if ("vertical".equals(strDirection)) direction = Image.SHEAR_VERTICAL;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "ImageShear", 3, "direction",
				"invalid direction definition [" + strDirection + "], " + "valid direction values are [horizontal,vertical]", null);

		// interpolation
		strInterpolation = strInterpolation.toLowerCase().trim();
		Object interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		if ("nearest".equals(strInterpolation)) interpolation = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
		else if ("bilinear".equals(strInterpolation)) interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		else if ("bicubic".equals(strInterpolation)) interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "ImageTranslate", 4, "interpolation",
				"invalid interpolation definition [" + strInterpolation + "], " + "valid interpolation values are [nearest,bilinear,bicubic]", null);

		img.shear((float) shear, direction, interpolation);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 4) return call(pc, args[0], cast.toDoubleValue(args[1]), cast.toString(args[2]), cast.toString(args[3]));
		if (args.length == 3) return call(pc, args[0], cast.toDoubleValue(args[1]), cast.toString(args[2]));
		if (args.length == 2) return call(pc, args[0], cast.toDoubleValue(args[1]));
		throw exp.createFunctionException(pc, "ImageShear", 2, 4, args.length);
	}
}