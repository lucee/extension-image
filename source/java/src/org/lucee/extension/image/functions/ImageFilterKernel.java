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

import java.awt.image.Kernel;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class ImageFilterKernel extends FunctionSupport {
	public static Object call(PageContext pc, double width, double height, Object oData) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		float[] data=null;
		if(oData instanceof float[])
			data=(float[]) oData;
		else if(eng.getDecisionUtil().isNativeArray(oData)) {
			data=toFloatArray(eng,pc,oData);
		}
		else if(eng.getDecisionUtil().isArray(oData)) {
			data=toFloatArray(eng,pc,eng.getCastUtil().toNativeArray(oData));
		}
		else 
			throw eng.getExceptionUtil().createFunctionException(pc, "", 3, "data", "cannot cast data to a float array",null);
		
		return new Kernel(eng.getCastUtil().toIntValue(width),eng.getCastUtil().toIntValue(height),data);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==3) return call(pc, cast.toDoubleValue(args[0]), cast.toDoubleValue(args[1]),args[2]);
		throw exp.createFunctionException(pc, "ImageFilterKernel", 3, 3, args.length);
	}

	private static float[] toFloatArray(CFMLEngine eng, PageContext pc,Object oData) throws PageException {
		float[] data=null;
		// Object[]
		if(oData instanceof Object[]) {
			Object[] arr = ((Object[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// boolean[]
		else if(oData instanceof boolean[]) {
			boolean[] arr = ((boolean[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// byte[]
		else if(oData instanceof byte[]) {
			byte[] arr = ((byte[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// short[]
		else if(oData instanceof short[]) {
			short[] arr = ((short[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// long[]
		else if(oData instanceof long[]) {
			long[] arr = ((long[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// int[]
		else if(oData instanceof int[]) {
			int[] arr = ((int[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		// double[]
		else if(oData instanceof double[]) {
			double[] arr = ((double[])oData);
			data=new float[arr.length];
			for(int i=0;i<arr.length;i++){
				data[i]=eng.getCastUtil().toFloatValue(arr[i]);
			}
		}
		else 
			throw eng.getExceptionUtil().
			createFunctionException(pc, "", 3, "data", "cannot cast data to a float array",null);
		
		return data;
	}
}