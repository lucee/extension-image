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

import lucee.runtime.type.Struct;
import lucee.runtime.util.Excepton;

public class ImageGetEXIFTag extends FunctionSupport {

	public static Object call(PageContext pc, Object name, String tagName) throws PageException {
		//if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc,name);
		
		Struct data = img.info();//ImageGetEXIFMetadata.getData(img);
		Object value = data.get(tagName, null);
		if(value==null){
			Excepton eu = CFMLEngineFactory.getInstance().getExceptionUtil();
			throw eu.createFunctionException(pc, "ImageGetEXIFTag", 2, "tagName", eu.similarKeyMessage(data.keys(),tagName,"tag","tags",null,true),null);
		}
		return value;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==2) return call(pc, args[0],cast.toString(args[1]));
		throw exp.createFunctionException(pc, "ImageGetEXIFTag", 2, 2, args.length);
	}
	
}