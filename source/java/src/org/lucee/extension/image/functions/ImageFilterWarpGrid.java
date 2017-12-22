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

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.filter.WarpGrid;


public class ImageFilterWarpGrid extends FunctionSupport {
	
	public static Object call(PageContext pc, double rows, double cols, double width, double height) {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		return new WarpGrid(eng.getCastUtil().toIntValue(rows), eng.getCastUtil().toIntValue(cols), eng.getCastUtil().toIntValue(width), eng.getCastUtil().toIntValue(height));
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==4) return call(pc, cast.toDoubleValue(args[0]), cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3]));
		throw exp.createFunctionException(pc, "ImageFilterWarpGrid", 4, 4, args.length);
	}
}