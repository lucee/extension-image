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
import java.util.HashSet;

import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.coder.Coder;
import org.lucee.extension.image.coder.MultiCoder;
import org.lucee.extension.image.format.FormatNames;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class ImageFormats extends FunctionSupport {

	private static final long serialVersionUID = -1031545954737979021L;

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Struct call(PageContext pc, boolean detailed) throws PageException {
		Struct sct = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
		Coder coder = Coder.getInstance();
		if (detailed && coder instanceof MultiCoder) {
			sct.set("decoder", ((MultiCoder) coder).getReaderFormatNamesByGroup());
			sct.set("encoder", ((MultiCoder) coder).getWriterFormatNamesByGroup());
		}
		else {
			try {
				if (coder instanceof FormatNames) {
					sct.set("decoder", toArray(((FormatNames) coder).getReaderFormatNames()));
					sct.set("encoder", toArray(((FormatNames) coder).getWriterFormatNames()));
				}
				else {
					sct.set("decoder", toArray(ImageUtil.getReaderFormatNames()));
					sct.set("encoder", toArray(ImageUtil.getWriterFormatNames()));
				}
			}
			catch (IOException e) {
				throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
			}
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 0) return call(pc);
		if (args.length == 1) return call(pc, CFMLEngineFactory.getInstance().getCastUtil().toBooleanValue(args[0]));
		throw exp.createFunctionException(pc, "ImageFormats", 0, 1, args.length);
	}

	private static Object toArray(String[] arr) {
		HashSet<String> set = new HashSet<>();
		for (int i = 0; i < arr.length; i++) {
			set.add(arr[i].toUpperCase());
		}
		return set.toArray(new String[set.size()]);
	}
}