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
import org.lucee.extension.image.coder.Coder;
import org.lucee.extension.image.coder.MultiCoder;
import org.lucee.extension.image.format.FormatNames;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

public class ImageFormats extends FunctionSupport {

	private static final long serialVersionUID = -1031545954737979021L;

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Struct call(PageContext pc, boolean detailed) throws PageException {

		Creation creator = CFMLEngineFactory.getInstance().getCreationUtil();
		Key DEC = creator.createKey("decoder");
		Key ENC = creator.createKey("encoder");

		Struct sct = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
		Coder coder = Coder.getInstance();
		if (detailed && coder instanceof MultiCoder) {
			sct.set(DEC, ((MultiCoder) coder).getReaderFormatNamesByGroup());
			sct.set(ENC, ((MultiCoder) coder).getWriterFormatNamesByGroup());
		}
		else {
			try {
				if (coder instanceof FormatNames) {
					sct.set(DEC, Coder.sortAndConvert(((FormatNames) coder).getReaderFormatNames()));
					sct.set(ENC, Coder.sortAndConvert(((FormatNames) coder).getWriterFormatNames()));
				}
				else {
					sct.set(DEC, Coder.sortAndConvert(ImageUtil.getReaderFormatNames()));
					sct.set(ENC, Coder.sortAndConvert(ImageUtil.getWriterFormatNames()));
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
}