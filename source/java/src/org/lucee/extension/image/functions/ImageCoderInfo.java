package org.lucee.extension.image.functions;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;
import org.lucee.extension.image.coder.Coder;
import org.lucee.extension.image.coder.MultiCoder;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Creation;

public class ImageCoderInfo extends FunctionSupport implements Function {

	private static final long serialVersionUID = 5401769192649626849L;

	public static Struct call(PageContext pc, Object source) throws PageException {
		MultiCoder mc = (MultiCoder) Coder.getInstance(pc);
		Resource src = CFMLEngineFactory.getInstance().getCastUtil().toResource(source);
		Creation cre = CFMLEngineFactory.getInstance().getCreationUtil();
		Struct data = cre.createStruct();
		try {
			String format = ImageUtil.getFormat(src);

			// read
			Array read = cre.createArray();
			data.set("read", read);
			BufferedImage bi = mc.read(src, null, read);

			// write
			Array write = cre.createArray();
			data.set("write", write);
			mc.write(new Image(bi), null, format, 1f, false, write);
		}
		catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		return data;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageCoderInfo", 1, 1, args.length);
	}
}