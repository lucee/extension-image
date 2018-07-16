package org.lucee.extension.image.functions;

import org.lucee.extension.image.Image;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class ImageGetIptcMetadata extends FunctionSupport {

	private static final long serialVersionUID = -8854843634077504751L;

	public static Struct call(PageContext pc, Object name) throws PageException {
		Image img = Image.toImage(pc,name);
		return img.getIPTCMetadata();
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==1) return call(pc, args[0]);
		throw exp.createFunctionException(pc, "ImageGetIptcMetadata", 1, 1, args.length);
	}

}
