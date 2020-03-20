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

import java.awt.Composite;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.lucee.extension.image.Image;
import org.lucee.extension.image.filter.*;
import org.lucee.extension.image.math.BinaryFunction;
import org.lucee.extension.image.math.Function2D;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

public class ImageFilter extends FunctionSupport {
	private static Struct _EMPTY_STRUCT;
	private static Map<String, Class> filters = new HashMap<String, Class>();
	static {
		filters.put("applymask", ApplyMaskFilter.class);
		filters.put("average", AverageFilter.class);
		filters.put("bicubicscale", BicubicScaleFilter.class);
		filters.put("block", BlockFilter.class);
		filters.put("blur", BlurFilter.class);
		filters.put("border", BorderFilter.class);
		filters.put("boxblur", BoxBlurFilter.class);
		filters.put("brushedmetal", BrushedMetalFilter.class);
		filters.put("bump", BumpFilter.class);
		filters.put("caustics", CausticsFilter.class);
		filters.put("cellular", CellularFilter.class);
		filters.put("channelmix", ChannelMixFilter.class);
		filters.put("check", CheckFilter.class);
		filters.put("chromakey", ChromaKeyFilter.class);
		filters.put("chrome", ChromeFilter.class);
		filters.put("circle", CircleFilter.class);
		//////// filters.put("composite",CompositeFilter.class);
		// filters.put("compound",CompoundFilter.class);
		filters.put("contour", ContourFilter.class);
		filters.put("contrast", ContrastFilter.class);
		filters.put("convolve", ConvolveFilter.class);
		filters.put("crop", CropFilter.class);
		filters.put("crystallize", CrystallizeFilter.class);
		filters.put("curl", CurlFilter.class);
		filters.put("curves", CurvesFilter.class);
		filters.put("despeckle", DespeckleFilter.class);
		filters.put("diffuse", DiffuseFilter.class);
		filters.put("diffusion", DiffusionFilter.class);
		filters.put("dilate", DilateFilter.class);
		filters.put("displace", DisplaceFilter.class);
		filters.put("dissolve", DissolveFilter.class);
		filters.put("dither", DitherFilter.class);
		filters.put("edge", EdgeFilter.class);
		filters.put("emboss", EmbossFilter.class);
		filters.put("equalize", EqualizeFilter.class);
		filters.put("erodealpha", ErodeAlphaFilter.class);
		filters.put("erode", ErodeFilter.class);
		filters.put("exposure", ExposureFilter.class);
		filters.put("fade", FadeFilter.class);
		filters.put("fbm", FBMFilter.class);
		filters.put("feedback", FeedbackFilter.class);
		filters.put("fieldwarp", FieldWarpFilter.class);
		filters.put("fill", FillFilter.class);
		filters.put("flare", FlareFilter.class);
		filters.put("flip", FlipFilter.class);
		filters.put("flush3d", Flush3DFilter.class);
		filters.put("fourcolor", FourColorFilter.class);
		filters.put("gain", GainFilter.class);
		filters.put("gamma", GammaFilter.class);
		filters.put("gaussian", GaussianFilter.class);
		filters.put("glint", GlintFilter.class);
		filters.put("glow", GlowFilter.class);
		filters.put("gradient", GradientFilter.class);
		filters.put("gradientwipe", GradientWipeFilter.class);
		filters.put("gray", GrayFilter.class);
		filters.put("grayscale", GrayscaleFilter.class);
		filters.put("halftone", HalftoneFilter.class);
		filters.put("hsbadjust", HSBAdjustFilter.class);
		filters.put("interpolate", InterpolateFilter.class);
		filters.put("invertalpha", InvertAlphaFilter.class);
		filters.put("invert", InvertFilter.class);
		// filters.put("iterated",IteratedFilter.class);
		filters.put("javalnf", JavaLnFFilter.class);
		filters.put("kaleidoscope", KaleidoscopeFilter.class);
		// filters.put("key",KeyFilter.class);
		filters.put("lensblur", LensBlurFilter.class);
		filters.put("levels", LevelsFilter.class);
		filters.put("life", LifeFilter.class);
		filters.put("light", LightFilter.class);
		filters.put("lookup", LookupFilter.class);
		filters.put("mapcolors", MapColorsFilter.class);
		filters.put("map", MapFilter.class);
		filters.put("marble", MarbleFilter.class);
		filters.put("marbletex", MarbleTexFilter.class);
		filters.put("mask", MaskFilter.class);
		filters.put("maximum", MaximumFilter.class);
		filters.put("median", MedianFilter.class);
		filters.put("minimum", MinimumFilter.class);
		filters.put("mirror", MirrorFilter.class);
		filters.put("motionblur", MotionBlurFilter.class);
		// filters.put("mutatable",MutatableFilter.class);
		filters.put("noise", NoiseFilter.class);
		filters.put("offset", OffsetFilter.class);
		filters.put("oil", OilFilter.class);
		filters.put("opacity", OpacityFilter.class);
		filters.put("outline", OutlineFilter.class);
		filters.put("perspective", PerspectiveFilter.class);
		filters.put("pinch", PinchFilter.class);
		filters.put("plasma", PlasmaFilter.class);
		filters.put("pointillize", PointillizeFilter.class);
		filters.put("polar", PolarFilter.class);
		filters.put("posterize", PosterizeFilter.class);
		// filters.put("premultiply",PremultiplyFilter.class);
		filters.put("quantize", QuantizeFilter.class);
		filters.put("quilt", QuiltFilter.class);
		filters.put("rays", RaysFilter.class);
		filters.put("reducenoise", ReduceNoiseFilter.class);
		filters.put("rendertext", RenderTextFilter.class);
		filters.put("rescale", RescaleFilter.class);
		filters.put("rgbadjust", RGBAdjustFilter.class);
		filters.put("ripple", RippleFilter.class);
		filters.put("rotate", RotateFilter.class);
		filters.put("saturation", SaturationFilter.class);
		filters.put("scale", ScaleFilter.class);
		filters.put("scratch", ScratchFilter.class);
		filters.put("shade", ShadeFilter.class);
		filters.put("shadow", ShadowFilter.class);
		filters.put("shape", ShapeFilter.class);
		filters.put("sharpen", SharpenFilter.class);
		filters.put("shatter", ShatterFilter.class);
		filters.put("shear", ShearFilter.class);
		filters.put("shine", ShineFilter.class);
		filters.put("skeleton", SkeletonFilter.class);
		// filters.put("sky",SkyFilter.class);
		filters.put("smartblur", SmartBlurFilter.class);
		filters.put("smear", SmearFilter.class);
		filters.put("solarize", SolarizeFilter.class);
		filters.put("sparkle", SparkleFilter.class);
		filters.put("sphere", SphereFilter.class);
		filters.put("stamp", StampFilter.class);
		filters.put("swim", SwimFilter.class);
		filters.put("texture", TextureFilter.class);
		filters.put("threshold", ThresholdFilter.class);
		filters.put("tileimage", TileImageFilter.class);
		// filters.put("transfer",TransferFilter.class);
		// filters.put("transform",TransformFilter.class);
		// filters.put("transition",TransitionFilter.class);
		filters.put("twirl", TwirlFilter.class);
		// filters.put("unpremultiply",UnpremultiplyFilter.class);
		filters.put("unsharp", UnsharpFilter.class);
		filters.put("variableblur", VariableBlurFilter.class);
		// filters.put("warp",WarpFilter.class);
		filters.put("water", WaterFilter.class);
		filters.put("weave", WeaveFilter.class);
		filters.put("wholeimage", WholeImageFilter.class);
		filters.put("wood", WoodFilter.class);
	}

	public static String call(PageContext pc, Object name, String filterName) throws PageException {
		return call(pc, name, filterName, null);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 3) return call(pc, args[0], cast.toString(args[1]), cast.toStruct(args[2]));
		if (args.length == 2) return call(pc, args[0], cast.toString(args[1]));
		throw exp.createFunctionException(pc, "ImageFilter", 2, 3, args.length);
	}

	public static String call(PageContext pc, Object name, String filterName, Struct parameters) throws PageException {
		// if(name instanceof String) name=pc.getVariable(Caster.toString(name));
		Image img = Image.toImage(pc, name);
		String lcFilterName = filterName.trim().toLowerCase();
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		// get filter class
		Class clazz = filters.get(lcFilterName);
		if (clazz == null) {
			String[] keys = filters.keySet().toArray(new String[filters.size()]);
			Arrays.sort(keys);
			String list = eng.getListUtil().toList(keys, ", ");

			throw eng.getExceptionUtil().createFunctionException(pc, "ImageFilter", 2, "filtername",
					"invalid filter name [" + filterName + "], valid filter names are [" + list + "]", null);
		}

		// load filter
		DynFiltering filter = null;
		try {
			filter = (DynFiltering) clazz.newInstance();
		}
		catch (Exception e) {
			throw eng.getCastUtil().toPageException(e);
		}

		// execute filter
		BufferedImage bi = img.getBufferedImage();
		// BufferedImage empty = bi;//ImageUtil.createBufferedImage(bi);
		img.image(filter.filter(bi, parameters == null ? getEmptyStruct() : parameters));

		return null;
	}

	private static Struct getEmptyStruct() {
		if (_EMPTY_STRUCT == null) _EMPTY_STRUCT = CFMLEngineFactory.getInstance().getCreationUtil().createStruct();
		return null;
	}

	private static void setters(String key, Class clazz, StringBuilder sb) {

		// sb.append("Object o;\n");
		sb.append("	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);\n");
		sb.append("		Object o;\n");

		Method[] methods = clazz.getMethods();
		Method method;
		StringBuilder names = new StringBuilder();
		for (int i = 0; i < methods.length; i++) {
			method = methods[i];
			if (method.getName().startsWith("set") && !method.getName().equals("setRGB") && !method.getName().equals("setDestination")) {
				String name = method.getName().substring(3);
				args(key, name, method, sb, i);
				if (names.length() > 0) names.append(", ");
				names.append(name);

			}
		}
		sb.append("\n");
		sb.append("		// check for arguments not supported\n");
		sb.append("		if(parameters.size()>0) {\n");
		sb.append(
				"			throw CFMLEngineFactory.getInstance().getExceptionUtil().createFunctionException(eng.getThreadPageContext(), \"ImageFilter\", 3, \"parameters\", \"the parameter\"+(parameters.size()>1?\"s\":\"\")+\" [\"+List.arrayToList(parameters.keys(),\", \")+\"] \"+(parameters.size()>1?\"are\":\"is\")+\" not allowed, only the following parameters are supported ["
						+ names + "]\");\n");
		sb.append("		}\n");
		sb.append("\n");

		sb.append("		return filter(src, dst);\n");
		sb.append("	}\n");

	}

	private static void args(String className, String name, Method method, StringBuilder sb, int methodIndex) {
		Class[] params = method.getParameterTypes();

		if (params.length == 1) {
			sb.append("		if((o=parameters.removeEL(eng.getCreationUtil().createKey(\"" + name + "\")))!=null)");
			sb.append(method.getName() + "(");
			arg(name, params[0], method, sb, methodIndex);

			sb.append(");\n");
		}
		else if (params.length == 2 && name.equals("Dimensions")) {
			sb.append("		if((o=parameters.removeEL(eng.getCreationUtil().createKey(\"" + name + "\")))!=null){\n");
			sb.append("			int[] dim=ImageFilterUtil.toDimensions(o,\"Dimensions\");\n");
			sb.append("			" + method.getName() + "(dim[0],dim[1]");
			sb.append(");\n");
			sb.append("		}\n");
		}
	}

	private static void arg(String name, Class param, Method method, StringBuilder sb, int methodIndex) {

		sb.append("ImageFilterUtil.");
		if (param == float.class) sb.append("toFloatValue");
		else if (param == boolean.class) sb.append("toBooleanValue");
		else if (param == int.class) sb.append("toIntValue");
		else if (param == Point2D.class) sb.append("toPoint2D");
		else if (param == WarpGrid.class) sb.append("toWarpGrid");
		else if (param == Kernel.class) sb.append("toKernel");
		else if (param == Colormap.class) sb.append("toColormap");
		else if (param == Function2D.class) sb.append("toFunction2D");
		else if (param == BufferedImage.class) sb.append("toBufferedImage");
		else if (param == BinaryFunction.class) sb.append("toBinaryFunction");
		else if (param == String.class) sb.append("toString");
		else if (param == Paint.class) sb.append("toPaint");
		else if (param == Font.class) sb.append("toFont");
		else if (param == AffineTransform.class) sb.append("toAffineTransform");
		else if (param == Composite.class) sb.append("toComposite");
		else if (param == LightFilter.Material.class) sb.append("toLightFilter$Material");
		// else if(param==FieldWarpFilter.Line.class) sb.append("toFieldWarpFilter$Line");
		else if (param == FieldWarpFilter.Line[].class) sb.append("toAFieldWarpFilter$Line");
		else if (param == CurvesFilter.Curve.class) sb.append("toCurvesFilter$Curve");
		else if (param == CurvesFilter.Curve[].class) sb.append("toACurvesFilter$Curve");
		else if (param == Point.class) sb.append("toPoint");

		else if (param == int[].class) sb.append("toAInt");
		else if (param == int[][].class) sb.append("toAAInt");
		else if (param == float[].class) sb.append("toAFloat");

		else {

		}
		sb.append("(o,\"" + name + "\")");

	}

}