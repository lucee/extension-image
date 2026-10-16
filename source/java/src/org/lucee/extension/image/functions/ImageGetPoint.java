package org.lucee.extension.image.functions;

import java.awt.Color;
import java.awt.image.BufferedImage;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

import org.lucee.extension.image.Image;

public class ImageGetPoint extends FunctionSupport {

	public static Struct call(PageContext pc, Object name, double x, double y) throws PageException {
		Image img = Image.toImage(pc,name);
		BufferedImage bi = img.getBufferedImage();
		
		Struct pixel = eng.getCreationUtil().createStruct();
		Color color = new Color( bi.getRGB( (int)x, (int)y ) );
		pixel.setEL("x", x);
		pixel.setEL("y", y);
		pixel.setEL("red", color.getRed());
		pixel.setEL("blue", color.getBlue());
		pixel.setEL("green", color.getGreen());
		pixel.setEL("alpha", color.getAlpha());
		pixel.setEL("hex", "#"+Integer.toHexString(color.getRGB()).substring(2));
		return pixel;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==3) return call(pc, args[0],cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]));
		throw exp.createFunctionException(pc, "ImageGetPoint", 3, 3, args.length);
	}
}
