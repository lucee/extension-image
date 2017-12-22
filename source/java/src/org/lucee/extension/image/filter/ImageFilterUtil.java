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
package org.lucee.extension.image.filter;import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;








import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.filter.LightFilter.Material;
import org.lucee.extension.image.math.Function2D;
import org.lucee.extension.image.util.ArrayUtil;

import lucee.runtime.type.Struct;
public class ImageFilterUtil {

	public static float toFloatValue(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		float res = eng.getCastUtil().toFloatValue(value,Float.NaN);
		if(Float.isNaN(res)) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
					msg(value,"float",argName),null);
		}
		return res;
	}

	public static int toIntValue(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		int res = eng.getCastUtil().toIntValue(value,Integer.MIN_VALUE);
		if(Integer.MIN_VALUE==res) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
					msg(value,"int",argName),null);
		}
		return res;
	}
	public static boolean toBooleanValue(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Boolean res = eng.getCastUtil().toBoolean(value,null);
		if(res==null) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
					msg(value,"boolean",argName),null);
		}
		return res;
	}
	public static String toString(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		String res = eng.getCastUtil().toString(value,null);
		if(res==null) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3,
					"parameters", msg(value,"String",argName),null);
		}
		return res;
	}
	


	public static BufferedImage toBufferedImage(Object o, String argName) throws PageException {
		if(o instanceof BufferedImage) return (BufferedImage) o;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		return Image.toImage(eng.getThreadPageContext(),o).getBufferedImage();
	}

	public static Colormap toColormap(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if(value instanceof Colormap)
			return (Colormap) value;
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				msg(value,"Colormap",argName)+" use function ImageFilterColorMap to create a colormap",null);
	}
	
	////

	public static Color toColor(Object value, String argName) throws PageException {
		if(value instanceof Color)
			return (Color) value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		return eng.getCastUtil().toColor(eng.getCastUtil().toString(value));
		
	}
	
	public static int toColorRGB(Object value, String argName) throws PageException {
		return toColor(value, argName).getRGB();
		
	}
	


	public static Point toPoint(Object value, String argName) throws PageException {
		if(value instanceof Point) return (Point) value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		String str = eng.getCastUtil().toString(value);
		
		Struct sct = eng.getCastUtil().toStruct(value,null);
		if(sct!=null){
			return new Point(eng.getCastUtil().toIntValue(sct.get("x")),eng.getCastUtil().toIntValue(sct.get("y")));
		}
		
		String[] arr = eng.getListUtil().toStringArray(str, ",");
		if(arr.length==2) {
			return new Point(eng.getCastUtil().toIntValue(arr[0]),eng.getCastUtil().toIntValue(arr[1]));
		}
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"use the following format [x,y]",null);
		
	}

	public static int[] toDimensions(Object value, String argName) throws PageException {
		return toAInt(value, argName);
	}

	public static LightFilter.Material toLightFilter$Material(Object value, String argName) throws PageException {
		if(value instanceof LightFilter.Material)
			return (LightFilter.Material) value;
		
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Struct sct = eng.getCastUtil().toStruct(value,null);
		if(sct!=null){
			Material material = new LightFilter.Material();
			material.setDiffuseColor(toColorRGB(sct.get("color"), argName+".color"));
			material.setOpacity(eng.getCastUtil().toFloatValue(sct.get("opacity")));
			return material;
		}
		String str = eng.getCastUtil().toString(value,null);
		if(str!=null){
			String[] arr = eng.getListUtil().toStringArray(str, ",");
			if(arr.length==2) {
				Material material = new LightFilter.Material();
				material.setDiffuseColor(toColorRGB(arr[0], argName+"[1]"));
				material.setOpacity(eng.getCastUtil().toFloatValue(arr[1]));
				return material;
			}
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
					"use the following format [color,opacity]",null);
			
		}
		
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"use the following format [\"color,opacity\"] or [{color='#cc0033',opacity=0.5}]",null);
		
	}

	public static Function2D toFunction2D(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"type Function2D not supported yet!",null);
	}


	public static AffineTransform toAffineTransform(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"type BufferedImage not supported yet!",null);
	}

	public static Composite toComposite(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"type Composite not supported yet!",null);
	}

	public static CurvesFilter.Curve[] toACurvesFilter$Curve(Object value, String argName) throws PageException {
		if(value instanceof CurvesFilter.Curve[]) return (CurvesFilter.Curve[]) value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Object[] arr = eng.getCastUtil().toNativeArray(value);
		CurvesFilter.Curve[] curves=new CurvesFilter.Curve[arr.length];
		for(int i=0;i<arr.length;i++){
			curves[i]=toCurvesFilter$Curve(arr[i],argName);
		}
		return curves;
	}

	public static CurvesFilter.Curve toCurvesFilter$Curve(Object value, String argName) throws PageException {
		if(value instanceof CurvesFilter.Curve)
			return (CurvesFilter.Curve) value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				msg(value,"Curve",argName)+" use function ImageFilterCurve to create a Curve",null);
	}

	public static int[] toAInt(Object value, String argName) throws PageException {
		return ArrayUtil.toIntArray(value);
	}

	public static float[] toAFloat(Object value, String argName) throws PageException {
		return ArrayUtil.toFloatArray(value);
	}

	public static int[][] toAAInt(Object value, String argName) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"type int[][] not supported yet!",null);
	}

	public static WarpGrid toWarpGrid(Object value, String argName) throws PageException {
		if(value instanceof WarpGrid)
			return (WarpGrid) value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				msg(value,"WarpGrid",argName)+" use function ImageFilterWarpGrid to create a WarpGrid",null);
	}

	public static FieldWarpFilter.Line[] toAFieldWarpFilter$Line(Object o, String string) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", 
				"type WarpGrid not supported yet!",null);
	}
	
	
	
	

	private static String msg(Object value, String type, String argName) {
		return "Can't cast argument ["+argName+"] to a value of type ["+type+"]";
	}

	public static Font toFont(Object o, String string) {
		// TODO Auto-generated method stub
		return null;
	}





	private static float range(float value, int from, int to) throws PageException {
		if(value>=from && value<=to)
			return value;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		throw eng.getExceptionUtil().createExpressionException("["+eng.getCastUtil().toString(value)+"] is out of range, value must be between ["+eng.getCastUtil().toString(from)+"] and ["+eng.getCastUtil().toString(to)+"]");
	}


}