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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.util.CommonUtil;

import lucee.runtime.type.Struct;


public class ImageGetEXIFMetadata extends FunctionSupport {

	public static Struct call(PageContext pc, Object name) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		return img.info();//getData(img);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageGetEXIFMetadata", 1, 1, args.length);
	}

	public static Struct flatten(Struct sct) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		Struct data=eng.getCreationUtil().createStruct();
		Iterator it = sct.entrySet().iterator();
		Map.Entry entry;
		while(it.hasNext()){
			entry=(Entry) it.next();
			if(entry.getValue() instanceof Map) 
				fill(data,(Map)entry.getValue());
			else if(entry.getValue() instanceof List) 
				fill(data,entry.getKey(),(List)entry.getValue());
			else
				data.put(entry.getKey(),unwrap(eng,entry.getValue()));
		}
		
		return data;
	}

	private static Object unwrap(CFMLEngine eng, Object value) {
		if(value instanceof CharSequence) return CommonUtil.unwrap(value.toString());
		return value;
	}

	private static void fill(Struct data, Map map) throws PageException {
		Iterator it = map.entrySet().iterator();
		Map.Entry entry;
		while(it.hasNext()){
			entry=(Entry) it.next();
			if(entry.getValue() instanceof Map) 
				fill(data,(Map)entry.getValue());
			else if(entry.getValue() instanceof List) 
				fill(data,entry.getKey(),(List)entry.getValue());
			else
				data.put(entry.getKey(),entry.getValue());
		}
	}

	private static void fill(Struct data, Object key, List list) throws PageException {
		data.put(
				key,
				CommonUtil.unwrap(CFMLEngineFactory.getInstance().getListUtil().toList(list, ",")));
	}
}