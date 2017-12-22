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

public class ImageDrawOval extends FunctionSupport {

	public static String call(PageContext pc, Object name, double x, double y, double width,double height) throws PageException {
		return call(pc, name, x, y, width, height,false);
	}
	public static String call(PageContext pc, Object name, double x, double y, double width,double height, boolean filled) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageDrawOval",3,"width","width must contain a none negative value",null);
		if (height < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageDrawOval",4,"height","width must contain a none negative value",null);
		
		img.drawOval((int)x, (int)y, (int)width, (int)height, filled);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==6) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3]),cast.toDoubleValue(args[4]),cast.toBooleanValue(args[5]));
		if(args.length==5) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3]),cast.toDoubleValue(args[4]));
		throw exp.createFunctionException(pc, "ImageDrawOval", 5, 6, args.length);
	}
	
}