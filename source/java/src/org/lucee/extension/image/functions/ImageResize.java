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

import org.lucee.extension.image.Image;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

public class ImageResize extends FunctionSupport implements Function {

    public static String call(PageContext pc, Object name) throws PageException {
	return call(pc, name, null, null, "automatic", 1.0);
    }

    public static String call(PageContext pc, Object name, String width) throws PageException {
	return call(pc, name, width, null, "automatic", 1.0);
    }

    public static String call(PageContext pc, Object name, String width, String height) throws PageException {
	return call(pc, name, width, height, "automatic", 1.0);
    }

    public static String call(PageContext pc, Object name, String width, String height, String interpolation) throws PageException {
	return call(pc, name, width, height, interpolation, 1.0);
    }

    public static String call(PageContext pc, Object name, String width, String height, String interpolation, double blurFactor) throws PageException {
	// image
	// if(name instanceof String)name=pc.getVariable(Caster.toString(name));
	Image image = Image.toImage(pc, name);
	image = Image.createImage(pc, name, false, false, true, null);
	interpolation = interpolation.toLowerCase().trim();

	if (blurFactor <= 0.0 || blurFactor > 10.0) throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "ImageResize", 5, "blurFactor",
		"argument blurFactor must be between 0 and 10", null);

	// MUST interpolation/blur
	// if(!"highestquality".equals(interpolation) || blurFactor!=1.0)throw new
	// ExpressionException("argument interpolation and blurFactor are not supported for function
	// ImageResize");

	image.resize(width, height, interpolation, blurFactor);
	return null;
    }

    @Override
    public Object invoke(PageContext pc, Object[] args) throws PageException {
	if (args.length == 5) return call(pc, args[0], cast.toString(args[1]), cast.toString(args[2]), cast.toString(args[3]), cast.toDoubleValue(args[4]));
	if (args.length == 4) return call(pc, args[0], cast.toString(args[1]), cast.toString(args[2]), cast.toString(args[3]));
	if (args.length == 3) return call(pc, args[0], cast.toString(args[1]), cast.toString(args[2]));
	if (args.length == 2) return call(pc, args[0], cast.toString(args[1]));
	if (args.length == 1) return call(pc, args[0]);
	throw exp.createFunctionException(pc, "ImageResize", 1, 5, args.length);
    }
}