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

public class ImageSharpen extends FunctionSupport {

	public static String call(PageContext pc, Object name) throws PageException {
		return call(pc, name,1.0);
	}
	public static String call(PageContext pc, Object name, double gain) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		if (gain < -1.0 || gain > 2.0)
		    throw CFMLEngineFactory.getInstance().getExceptionUtil()
		    .createFunctionException(pc,"ImageSharpen",2,"gain","value must be between 1 and 2",null);
		img.sharpen((float)gain);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==2) return call(pc, args[0],cast.toDoubleValue(args[1]));
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageSharpen", 1, 2, args.length);
	}
}