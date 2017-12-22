package org.lucee.extension.image.functions;

import java.io.IOException;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.ImageUtil;

import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public class ImageWriteToBrowser extends FunctionSupport {

	public static String call(PageContext pc, Object name) throws PageException {
		Image source=Image.toImage(pc,name);
		
		// TODO add base64 and passthrough
		
		String b64 = source.getBase64String("png");
		try {
			pc.write("<img src=\"data:"+ImageUtil.getMimeTypeFromFormat("png")+";base64,"+b64+"\" width=\""+
					source.getWidth()+"\" height=\""+source.getHeight()+"\" />");
		} catch (IOException e) {
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
		return null;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageWriteToBrowser", 1, 1, args.length);
	}
}