package org.lucee.extension.image.util;

import lucee.loader.engine.CFMLEngineFactory;

public class IDGenerator {
	
	private static int id;

	public static synchronized int intId(){
		id++;
		if(id==Integer.MAX_VALUE) id=0;
		return id;
	}
	/*public static synchronized String stringId(){
		return new StringBuilder("id-").append(CFMLEngineFactory.getInstance().getStringUtil().addZeros(intId(), 20)).toString();
	}*/
}