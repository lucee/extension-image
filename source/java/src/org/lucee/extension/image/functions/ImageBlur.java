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

import org.lucee.extension.image.Image;

public class ImageBlur extends FunctionSupport {


	public static String call(PageContext pc, Object oimg) throws PageException {
		return call(pc,oimg,3d);
	}
	
	public static String call(PageContext pc, Object oimg, double blurFactor) throws PageException {
		
		if(blurFactor<3 || blurFactor>10) {
			CFMLEngine e = CFMLEngineFactory.getInstance();
			throw e.getExceptionUtil()
			.createFunctionException(pc,"ImageBlur",2,"blurFactor","invalid value ["+e.getCastUtil().toString(blurFactor)+"], value have to be between 3 and 10",null);
		}
		Image.toImage(oimg).blur((int)blurFactor);
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==2) return call(pc, args[0],cast.toDoubleValue(args[1]));
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageBlur", 1, 2, args.length);
	}
}