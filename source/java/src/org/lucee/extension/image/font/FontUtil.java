package org.lucee.extension.image.font;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Array;

public class FontUtil {

	private static Array fonts;
	private static Graphics2D graphics;
	private static Object sync=new SerializableObject();

	public static Array getAvailableFontsAsStringArray() {
		Iterator<Object> it = getAvailableFonts(false).valueIterator();
		Array arr=CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		while(it.hasNext()) {
			arr.appendEL(((Font)it.next()).getFontName());
		}
		return arr;
	}
	private static Array getAvailableFonts(boolean duplicate) {
		synchronized (sync) {
			if (fonts == null) {
				fonts = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
					GraphicsEnvironment graphicsEvn = GraphicsEnvironment.getLocalGraphicsEnvironment();
					Font[] availableFonts = graphicsEvn.getAllFonts();
					for (int i = 0; i < availableFonts.length; i++) {
						fonts.appendEL(availableFonts[i]);
					}
				
			}
			if(!duplicate) return fonts;
			return (Array) duplicate(fonts);
		}
	}

	private static Array duplicate(Array src) {
		Array trg = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		Iterator<Object> it = src.valueIterator();
		while(it.hasNext()) {
			trg.appendEL(it.next());
		}
		return trg;
	}
	public static String toString(Font font) {
		if(font==null) return null;
		return font.getFontName();
	}

	public static Font getFont(String font, Font defaultValue) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();

		/* check if given font is a path to a ttf file (check extension, then check existence) */
		if(!eng.getStringUtil().isEmpty(font) && font.length() > 4
				&& font.substring(font.length()-4).toLowerCase() == ".ttf") {

			/* Check if the font is a valid path */
			Resource res;
			try {
				res = eng.getCastUtil().toResource(font);
				if (res.exists() && res.isFile()) {
					eng.getThreadPageContext().getConfig().getSecurityManager().checkFileLocation(res);
				}
			} catch (PageException ee) {
				res = null;
			}

			if (res != null) {
				/* return the font */
				try {
					return Font.createFont(Font.TRUETYPE_FONT, res.getInputStream());
				} catch(IOException e) {
					throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
							"Font file could not be read"
							,"The font file at ["+res.getAbsolutePath()+"] could not be read: " + e.getMessage());
				} catch(FontFormatException e) {
					throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException(
							"Invalid ttf font file"
							,"A FontFormat exception occurred while trying to use the font file at" +
									" [" + res.getAbsolutePath() + "]. " +
									"Make sure the file is a TrueType font. Exception: " + e.getMessage());
				}
			}
		}

		Font f=Font.decode(font);
		if(f!=null) return f;
		// font name
		Iterator<Object> it = getAvailableFonts(false).valueIterator();
		while(it.hasNext()) {
			f=(Font) it.next();
			if(f.getFontName().equalsIgnoreCase(font)) return f;
		}
		// family
		it = getAvailableFonts(false).valueIterator();
		while(it.hasNext()) {
			f=(Font) it.next();
			if(f.getFamily().equalsIgnoreCase(font)) return f;
		}
		return defaultValue;
	}
	
	public static Font getFont(String font) throws PageException {
		Font f = getFont(font,null);
		if(f!=null) return f;
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createExpressionException("no font with name ["+font+"] available"
	    		,"to get available fonts call function ImageFonts()");
	}
	
	public static FontMetrics getFontMetrics(Font font) {
		if(graphics==null) {
			graphics = new BufferedImage(1, 1,BufferedImage.TYPE_INT_ARGB).createGraphics();
		}
		return graphics.getFontMetrics(font);
	}
}
class SerializableObject implements Serializable {
	
}