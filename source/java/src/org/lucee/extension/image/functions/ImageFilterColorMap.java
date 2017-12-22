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

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.filter.GrayscaleColormap;
import org.lucee.extension.image.filter.LinearColormap;
import org.lucee.extension.image.filter.SpectrumColormap;

public class ImageFilterColorMap extends FunctionSupport {
	public static Object call(PageContext pc, String type) throws PageException {
		return call(pc, type, null, null);
	}
	public static Object call(PageContext pc, String type, String lineColor1) throws PageException {
		return call(pc, type, lineColor1, null);
		
	}
	public static Object call(PageContext pc, String type, String lineColor1,String lineColor2) throws PageException {
		type=type.toLowerCase().trim();
		
		if("grayscale".equals(type)) return new GrayscaleColormap();
		else if("spectrum".equals(type)) return new SpectrumColormap();
		else if("linear".equals(type)) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			boolean isEmpty1=eng.getStringUtil().isEmpty(lineColor1);
			boolean isEmpty2=eng.getStringUtil().isEmpty(lineColor2);
			
			if(isEmpty1 && isEmpty2) return new LinearColormap();
			else if(!isEmpty1 && !isEmpty2) {
				Color color1 = eng.getCastUtil().toColor(lineColor1);
				Color color2 = eng.getCastUtil().toColor(lineColor2);
				return new LinearColormap(color1.getRGB(),color2.getRGB());
			}
			else 
				throw CFMLEngineFactory.getInstance().getExceptionUtil()
				.createFunctionException(pc, "ImageFilterColorMap", 2, "lineColor1", "when you define linecolor1 you have to define linecolor2 as well",null);
				
		}
		else throw CFMLEngineFactory.getInstance().getExceptionUtil()
		.createFunctionException(pc, "ImageFilterColorMap", 1, "type", "invalid type defintion, valid types are [grayscale,spectrum,linear]",null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==3) return call(pc, cast.toString(args[0]),cast.toString(args[1]),cast.toString(args[2]));
		if(args.length==2) return call(pc, cast.toString(args[0]),cast.toString(args[1]));
		if(args.length==1) return call(pc, cast.toString(args[0]));
		throw exp.createFunctionException(pc, "ImageFilterColorMap", 1, 3, args.length);
	}
}