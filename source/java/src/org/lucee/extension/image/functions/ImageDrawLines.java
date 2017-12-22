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

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.Image;

import lucee.runtime.type.Array;
import lucee.runtime.util.Cast;

public class ImageDrawLines extends FunctionSupport {

	public static String call(PageContext pc, Object name, Array xcoords, Array ycoords) throws PageException {
		return call(pc, name, xcoords, ycoords, false, false);
	}

	public static String call(PageContext pc, Object name, Array xcoords, Array ycoords, boolean isPolygon) throws PageException {
		return call(pc, name, xcoords, ycoords, isPolygon, false);
	}

	public static String call(PageContext pc, Object name, Array xcoords, Array ycoords, boolean isPolygon, boolean filled) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if(xcoords.size()!=ycoords.size())
			throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("xcoords and ycoords has not the same size");
		img.drawLines(toIntArray(xcoords), toIntArray(ycoords), isPolygon, filled);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==5) return call(pc, args[0],cast.toArray(args[1]),cast.toArray(args[2]),cast.toBooleanValue(args[3]),cast.toBooleanValue(args[4]));
		if(args.length==4) return call(pc, args[0],cast.toArray(args[1]),cast.toArray(args[2]),cast.toBooleanValue(args[3]));
		if(args.length==3) return call(pc, args[0],cast.toArray(args[1]),cast.toArray(args[2]));
		throw exp.createFunctionException(pc, "ImageDrawLines", 3, 5, args.length);
	}

	private static int[] toIntArray(Array arr) throws PageException {
		Cast caster = CFMLEngineFactory.getInstance().getCastUtil();
		int[] iarr=new int[arr.size()];
		for(int i=0;i<iarr.length;i++) {
			iarr[i]=caster.toIntValue(arr.getE(i+1));
		}
		return iarr;
	}
	
}