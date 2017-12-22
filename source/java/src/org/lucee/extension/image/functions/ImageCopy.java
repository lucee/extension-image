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

public class ImageCopy extends FunctionSupport {

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageCopy",3,"width","width must contain a none negative value",null);
		if (height < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageCopy",4,"height","width must contain a none negative value",null);
		
		return img.copy((float)x, (float)y, (float)width, (float)height);
	}

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height,double dx) throws PageException {
		throw CFMLEngineFactory.getInstance().getExceptionUtil()
		.createFunctionException(pc,"ImageCopy",7,"dy","when you define dx, you have also to define dy",null);
	}

	public static Object call(PageContext pc, Object name, double x, double y, double width, double height, double dx, double dy) throws PageException {
		if(dx==-999 && dy==-999){// -999 == default value for named argument
			return call(pc, name, x, y, width, height);
		}
		if(dx==-999){// -999 == default value for named argument
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
			.createFunctionException(pc,"ImageCopy",6,"dx","when you define dy, you have also to define dx",null);
		}
		if(dy==-999){// -999 == default value for named argument
			throw CFMLEngineFactory.getInstance().getExceptionUtil()
			.createFunctionException(pc,"ImageCopy",7,"dy","when you define dx, you have also to define dy",null);
		}
		
		
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (width < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageCopy",3,"width","width must contain a none negative value",null);
		if (height < 0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageCopy",4,"height","width must contain a none negative value",null);
		
		return img.copy((float)x, (float)y, (float)width, (float)height, (float)dx, (float)dy);
		//return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==7) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3])
				,cast.toDoubleValue(args[4]),cast.toDoubleValue(args[5]),cast.toDoubleValue(args[6]));
		if(args.length==6) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3])
				,cast.toDoubleValue(args[4]),cast.toDoubleValue(args[5]));
		if(args.length==5) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toDoubleValue(args[3])
				,cast.toDoubleValue(args[4]));
		throw exp.createFunctionException(pc, "ImageCopy", 5, 7, args.length);
	}	
}