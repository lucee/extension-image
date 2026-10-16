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

import java.io.IOException;

import org.lucee.extension.image.ImageUtil;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class GetWriteableImageFormats extends FunctionSupport {

	private static final long serialVersionUID = -60088825150776253L;

	public static String call(PageContext pc) throws PageException {
		try {
			return GetReadableImageFormats.format(add(ImageUtil.getWriterFormatNames(), "gif"));
		}
		catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private static String[] add(String[] formats, String value) {
		String[] rtn = new String[formats.length + 1];
		for (int i = 0; i < formats.length; i++) {
			rtn[i] = formats[i];
		}
		rtn[formats.length] = value;
		return rtn;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		throw exp.createFunctionException(pc, "GetWriteableImageFormats", 0, 0, args.length);
	}
}