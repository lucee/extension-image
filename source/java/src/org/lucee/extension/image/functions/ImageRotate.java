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
import lucee.runtime.ext.function.Function;
import lucee.runtime.util.Cast;

import org.lucee.extension.image.Image;


public class ImageRotate extends FunctionSupport implements Function {
	
	public static String call(PageContext pc, Object name, String angle) throws PageException {
		return _call(pc, name,-1F,-1F,CFMLEngineFactory.getInstance().getCastUtil().toFloatValue(angle),"nearest");
	}
	
	public static String call(PageContext pc, Object name, String angle, String strInterpolation) throws PageException {
		return _call(pc, name,-1F,-1F,CFMLEngineFactory.getInstance().getCastUtil().toFloatValue(angle),strInterpolation);
	}
	
	public static String call(PageContext pc, Object name, String x, String y, String angle) throws PageException {
		Cast c = CFMLEngineFactory.getInstance().getCastUtil();
		return _call(pc, name,c.toFloatValue(x),c.toFloatValue(y),c.toFloatValue(angle),"nearest");
	}

	public static String call(PageContext pc, Object name, String x, String y, String angle, String strInterpolation) throws PageException {
		Cast c = CFMLEngineFactory.getInstance().getCastUtil();
		return _call(pc, name,c.toFloatValue(x),c.toFloatValue(y),c.toFloatValue(angle),strInterpolation);
	}

	private static String _call(PageContext pc, Object name, float x, float y, float angle, String strInterpolation) throws PageException {
		
		Image img = Image.toImage(pc,name);
		strInterpolation=strInterpolation.trim().toLowerCase();
		int interpolation;
		if("nearest".equals(strInterpolation)) interpolation=org.lucee.extension.image.Image.INTERPOLATION_NEAREST;
		else if("bilinear".equals(strInterpolation)) interpolation=org.lucee.extension.image.Image.INTERPOLATION_BILINEAR;
		else if("bicubic".equals(strInterpolation)) interpolation=org.lucee.extension.image.Image.INTERPOLATION_BICUBIC;
		else if("none".equals(strInterpolation)) interpolation=org.lucee.extension.image.Image.INTERPOLATION_NONE;
		else throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("invalid interpolation definition ["+strInterpolation+"]," +
				" valid values are [nearest,bilinear,bicubic]");
		
		img.rotate(x,y,angle,interpolation);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==5) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]),cast.toString(args[3]),cast.toString(args[4]));
		if(args.length==4) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]),cast.toString(args[3]));
		if(args.length==3) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]));
		if(args.length==2) return call(pc, args[0],cast.toString(args[1]));
		throw exp.createFunctionException(pc, "ImageRotate", 2, 5, args.length);
	}
}