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
import java.awt.image.BufferedImage;








import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.Image;


public class ImageNew extends FunctionSupport {


	public static Object call(PageContext pc) {
		return new Image();
	}
	
	public static Object call(PageContext pc, Object source) throws PageException {
		if(isEmpty(source))
			return call(pc);//throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(pc,"ImageNew",1,"source","missing argument");
		return Image.createImage(pc, source, true,true,true,null);
	}
	
	private static boolean isEmpty(Object source) {
		if(source==null) return true;
		if(source instanceof CharSequence) return ((CharSequence)source).length()==0;
		return false;
	}

	public static Object call(PageContext pc,Object source, String width) throws PageException {
		return call(pc, source, width, null, null, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height) throws PageException {
		return call(pc, source, width, height, null, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height, String strImageType) throws PageException {
		return call(pc, source, width, height, strImageType, null);
	}
	
	public static Object call(PageContext pc,Object source, String width, String height, String strImageType, String strCanvasColor) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if(source==null)
			return call(pc);
		if(eng.getStringUtil().isEmpty(width) && eng.getStringUtil().isEmpty(height))
			return call(pc,source);
		
		if(eng.getStringUtil().isEmpty(width))
			throw eng.getExceptionUtil().createFunctionException(pc,"ImageNew",2,"width","missing argument",null);
		if(eng.getStringUtil().isEmpty(height))
			throw eng.getExceptionUtil().createFunctionException(pc,"ImageNew",3,"height","missing argument",null);
			
		if(!isEmpty(source))
			throw eng.getExceptionUtil().createFunctionException(pc,"ImageNew",1,"source","if you define width and height, source has to be empty",null);
		
		// image type
		int imageType;
		if(eng.getStringUtil().isEmpty(strImageType,true)) imageType=BufferedImage.TYPE_INT_RGB;
		else {
			strImageType=strImageType.trim().toLowerCase();
			if("rgb".equals(strImageType)) imageType=BufferedImage.TYPE_INT_RGB;
			else if("argb".equals(strImageType)) imageType=BufferedImage.TYPE_INT_ARGB;
			else if("gray".equals(strImageType)) imageType=BufferedImage.TYPE_BYTE_GRAY;
			else if("grayscale".equals(strImageType)) imageType=BufferedImage.TYPE_BYTE_GRAY;
			else throw eng.getExceptionUtil().createFunctionException(pc,"ImageNew",4,"imageType","imageType has an invalid value ["+strImageType+"]," +
				"valid values are [rgb,argb,grayscale]",null);
		}
		// canvas color
		Color canvasColor;
		if(eng.getStringUtil().isEmpty(strCanvasColor,true)) canvasColor=null;
		else canvasColor=eng.getCastUtil().toColor(strCanvasColor);
		
		return new Image(eng.getCastUtil().toIntValue(width),eng.getCastUtil().toIntValue(height), imageType,canvasColor);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==5) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]),cast.toString(args[3]),cast.toString(args[4]));
		if(args.length==4) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]),cast.toString(args[3]));
		if(args.length==3) return call(pc, args[0],cast.toString(args[1]),cast.toString(args[2]));
		if(args.length==2) return call(pc, args[0],cast.toString(args[1]));
		if(args.length==1) return call(pc, args[0]);
		if(args.length==0) return call(pc);
		throw exp.createFunctionException(pc, "ImageNew", 0, 5, args.length);
	}
}