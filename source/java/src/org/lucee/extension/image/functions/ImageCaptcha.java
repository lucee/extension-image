package org.lucee.extension.image.functions;

import java.awt.Color;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Array;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.MarpleCaptcha;
import org.lucee.extension.image.captcha.CaptchaException;
import org.lucee.extension.image.util.CommonUtil;

public class ImageCaptcha extends FunctionSupport implements Function {
	
	
	private static final String[] DEFAULT_FONTS = new String[]{"arial"};

	public static Object call(PageContext pc, String text, double height, double width, String strDifficulty, Object oFonts, double dFontSize, String strFontColor) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		
		String[] fonts=toFonts(oFonts);
		int fontSize=toFontSize(dFontSize);
		Color fontColor=toFontColor(strFontColor);
		int difficulty = toDifficulty(strDifficulty);
		if(width<=0 && height<=0) throw eng.getExceptionUtil().createApplicationException("A captcha requires width or height to be specified.");
		
		try {
			MarpleCaptcha c=new MarpleCaptcha();
			return new org.lucee.extension.image.Image(c.generate(text, (int)width,(int)height, 
					fonts, true, 
					fontColor,fontSize,difficulty));
		}
		catch (CaptchaException e) {
			throw eng.getCastUtil().toPageException(e);
		}
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if(args.length==7) return call(pc, cast.toString(args[0]),cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toString(args[3]), args[4],cast.toDoubleValue(args[5]),cast.toString(args[6]));
		if(args.length==6) return call(pc, cast.toString(args[0]),cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toString(args[3]), args[4],cast.toDoubleValue(args[5]),null);
		if(args.length==5) return call(pc, cast.toString(args[0]),cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toString(args[3]), args[4],0,null);
		if(args.length==4) return call(pc, cast.toString(args[0]),cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),cast.toString(args[3]), null,0,null);
		if(args.length==3) return call(pc, cast.toString(args[0]),cast.toDoubleValue(args[1]),cast.toDoubleValue(args[2]),null, null,0,null);
		throw exp.createFunctionException(pc, "ImageCaptcha", 3, 7, args.length);
	}
	
	private static Color toFontColor(String strFontColor) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if(eng.getStringUtil().isEmpty(strFontColor))	return Color.BLACK;
		return eng.getCastUtil().toColor(strFontColor);
	}

	public static int toDifficulty(String strDifficulty) throws PageException {
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if(eng.getStringUtil().isEmpty(strDifficulty,true))	return MarpleCaptcha.DIFFICULTY_LOW;
		
		strDifficulty=strDifficulty.trim().toLowerCase();
		if("low".equals(strDifficulty))	return MarpleCaptcha.DIFFICULTY_LOW;
		if("medium".equals(strDifficulty))	return MarpleCaptcha.DIFFICULTY_MEDIUM;
		if("high".equals(strDifficulty))	return MarpleCaptcha.DIFFICULTY_HIGH;
		
		throw eng.getExceptionUtil().createApplicationException("Unsupported captcha difficulty level ["+strDifficulty+"], " +
		"supported difficulty levels are [low,medium,high]");
		
	}

	public static int toFontSize(double dFontSize) {
		return dFontSize<=0?24:(int)dFontSize;
	}

	public static String[] toFonts(Object oFonts) throws PageException {
		if(oFonts==null) return DEFAULT_FONTS;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if(eng.getDecisionUtil().isArray(oFonts)) {
			Array arr = eng.getCastUtil().toArray(oFonts);
			if(arr!=null && arr.size()>0) {
				return eng.getListUtil().toStringArray(arr);
			}
			return DEFAULT_FONTS;
		}
		if(eng.getDecisionUtil().isSimpleValue(oFonts)){
			String str=eng.getCastUtil().toString(oFonts);
			if(eng.getStringUtil().isEmpty(str,true)) {
				return CommonUtil.trim(eng.getListUtil().toStringArray(eng.getListUtil().toArray(str.trim(), ",")));
			}
			return DEFAULT_FONTS;
		}
		throw eng.getExceptionUtil().createApplicationException("Cannot find captcha font ["+oFonts.getClass().getName()+"]");
	}
}
