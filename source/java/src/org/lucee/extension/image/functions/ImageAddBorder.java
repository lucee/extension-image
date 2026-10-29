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

import java.awt.Color;

import org.lucee.extension.image.Image;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

public class ImageAddBorder extends FunctionSupport implements Function {
	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc, name, 1D, "black", "constant", false);
	}

	public static String call(PageContext pc, Object name, double thickness) throws PageException {
		return call(pc, name, thickness, "black", "constant", false);
	}

	public static String call(PageContext pc, Object name, double thickness, String color) throws PageException {
		return call(pc, name, thickness, color, "constant", false);
	}

	public static String call(PageContext pc, Object name, double thickness, String color, String strBorderType) throws PageException {
		return call(pc, name, thickness, color, strBorderType, false);
	}

	public static String call(PageContext pc, Object name, double thickness, String color, String strBorderType, boolean inside) throws PageException {
		// if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		strBorderType = strBorderType.trim().toLowerCase();
		int borderType = Image.BORDER_CONSTANT;
		if ("zero".equals(strBorderType)) borderType = Image.BORDER_ZERO;
		else if ("constant".equals(strBorderType)) borderType = Image.BORDER_CONSTANT;
		else if ("copy".equals(strBorderType)) borderType = Image.BORDER_COPY;
		else if ("reflect".equals(strBorderType)) borderType = Image.BORDER_REFLECT;
		else if ("wrap".equals(strBorderType)) borderType = Image.BORDER_WRAP;

		Image image = Image.toImage(pc, name);
		Color border;
		if ("transparent".equalsIgnoreCase(color.trim())) border = new Color(0, 0, 0, 0);
		else border = CFMLEngineFactory.getInstance().getCastUtil().toColor(color);
		image.addBorder((int) thickness, border, borderType, inside);

		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 5) return call(pc, args[0], cast.toDoubleValue(args[1]), cast.toString(args[2]), cast.toString(args[3]), cast.toBooleanValue(args[4]));
		if (args.length == 4) return call(pc, args[0], cast.toDoubleValue(args[1]), cast.toString(args[2]), cast.toString(args[3]));
		if (args.length == 3) return call(pc, args[0], cast.toDoubleValue(args[1]), cast.toString(args[2]));
		if (args.length == 2) return call(pc, args[0], cast.toDoubleValue(args[1]));
		if (args.length == 1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageAddBorder", 1, 5, args.length);
	}
}
