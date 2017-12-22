package org.lucee.extension.image.util;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;
import lucee.runtime.util.Cast;

public class ArrayUtil {

	public static int[] toIntArray(Object obj) throws PageException {
		if(obj instanceof int[]) return (int[]) obj;
		Cast cu = CFMLEngineFactory.getInstance().getCastUtil();
		Array arr = cu.toArray(obj);
		int[] tarr=new int[arr.size()];
		for(int i=0;i<tarr.length;i++) {
			tarr[i]=cu.toIntValue(arr.getE(i+1));
		}
		return tarr;
	}
	public static float[] toFloatArray(Object obj) throws PageException {
		if(obj instanceof float[]) return (float[]) obj;
		Cast cu = CFMLEngineFactory.getInstance().getCastUtil();
		Array arr = cu.toArray(obj);
		float[] tarr=new float[arr.size()];
		for(int i=0;i<tarr.length;i++) {
			tarr[i]=cu.toFloatValue(arr.getE(i+1));
		}
		return tarr;
	}

}
