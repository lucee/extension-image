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

public class ImageFlip extends FunctionSupport {
	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc, name, "vertical");
	}

	public static String call(PageContext pc, Object name, String strTranspose) throws PageException {
		// if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc, name);

		strTranspose = strTranspose.toLowerCase().trim();
		int transpose = Image.TRANSPOSE_VERTICAL;
		if ("vertical".equals(strTranspose)) transpose = Image.TRANSPOSE_VERTICAL;
		else if ("horizontal".equals(strTranspose)) transpose = Image.TRANSPOSE_HORIZONTAL;
		else if ("diagonal".equals(strTranspose)) transpose = Image.TRANSPOSE_DIAGONAL;
		else if ("antidiagonal".equals(strTranspose)) transpose = Image.TRANSPOSE_ANTIDIAGONAL;
		else if ("anti diagonal".equals(strTranspose)) transpose = Image.TRANSPOSE_ANTIDIAGONAL;
		else if ("anti-diagonal".equals(strTranspose)) transpose = Image.TRANSPOSE_ANTIDIAGONAL;
		else if ("anti_diagonal".equals(strTranspose)) transpose = Image.TRANSPOSE_ANTIDIAGONAL;
		else if ("90".equals(strTranspose)) transpose = Image.TRANSPOSE_ROTATE_90;
		else if ("180".equals(strTranspose)) transpose = Image.TRANSPOSE_ROTATE_180;
		else if ("270".equals(strTranspose)) transpose = Image.TRANSPOSE_ROTATE_270;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc, "ImageFlip", 2, "transpose",
				"invalid transpose definition [" + strTranspose + "], " + "valid transpose values are [vertical,horizontal,diagonal,90,180,270]", null);

		img.flip(transpose);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 2) return call(pc, args[0], cast.toString(args[1]));
		if (args.length == 1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageFlip", 1, 2, args.length);
	}
}