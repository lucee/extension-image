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
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.lucee.extension.image.filter;import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;




import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.ImageUtil;

import lucee.runtime.type.Struct;


/**
 * A BufferedImageOp which combines two other BufferedImageOps, one after the other.
 */
public class CompoundFilter extends AbstractBufferedImageOp  implements DynFiltering {
	private BufferedImageOp filter1;
	private BufferedImageOp filter2;
	
	/**
     * Construct a CompoundFilter.
     * @param filter1 the first filter
     * @param filter2 the second filter
     */
    public CompoundFilter( BufferedImageOp filter1, BufferedImageOp filter2 ) {
		this.filter1 = filter1;
		this.filter2 = filter2;
	}
	
	@Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		BufferedImage image = filter1.filter( src, dst );
		image = filter2.filter( image, dst );
		return image;
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		//Object o;

		// check for arguments not supported
		if(parameters.size()>0) {
			CFMLEngine eng = CFMLEngineFactory.getInstance();
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+eng.getListUtil().toList(parameters.keys(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported []",null);
		}

		return filter(src, dst);
	}
}