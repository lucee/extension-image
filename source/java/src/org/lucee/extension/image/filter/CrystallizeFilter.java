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
/*
*

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.lucee.extension.image.filter;import java.awt.image.BufferedImage;




import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.exp.PageException;

import org.lucee.extension.image.ImageUtil;

import lucee.runtime.type.Struct;




/**
 * A filter which applies a crystallizing effect to an image, by producing Voronoi cells filled with colours from the image.
 */
public class CrystallizeFilter extends CellularFilter  implements DynFiltering {

	private float edgeThickness = 0.4f;
	private boolean fadeEdges = false;
	private int edgeColor = 0xff000000;

	public CrystallizeFilter() {
		setScale(16);
		setRandomness(0.0f);
	}
	
	public void setEdgeThickness(float edgeThickness) {
		this.edgeThickness = edgeThickness;
	}

	public float getEdgeThickness() {
		return edgeThickness;
	}

	public void setFadeEdges(boolean fadeEdges) {
		this.fadeEdges = fadeEdges;
	}

	public boolean getFadeEdges() {
		return fadeEdges;
	}

	public void setEdgeColor(int edgeColor) {
		this.edgeColor = edgeColor;
	}

	public int getEdgeColor() {
		return edgeColor;
	}

	@Override
	public int getPixel(int x, int y, int[] inPixels, int width, int height) {
		float nx = m00*x + m01*y;
		float ny = m10*x + m11*y;
		nx /= scale;
		ny /= scale * stretch;
		nx += 1000;
		ny += 1000;	// Reduce artifacts around 0,0
		float f = evaluate(nx, ny);

		float f1 = results[0].distance;
		float f2 = results[1].distance;
		int srcx = ImageMath.clamp((int)((results[0].x-1000)*scale), 0, width-1);
		int srcy = ImageMath.clamp((int)((results[0].y-1000)*scale), 0, height-1);
		int v = inPixels[srcy * width + srcx];
		f = (f2 - f1) / edgeThickness;
		f = ImageMath.smoothStep(0, edgeThickness, f);
		if (fadeEdges) {
			srcx = ImageMath.clamp((int)((results[1].x-1000)*scale), 0, width-1);
			srcy = ImageMath.clamp((int)((results[1].y-1000)*scale), 0, height-1);
			int v2 = inPixels[srcy * width + srcx];
			v2 = ImageMath.mixColors(0.5f, v2, v);
			v = ImageMath.mixColors(f, v2, v);
		} else
			v = ImageMath.mixColors(f, edgeColor, v);
		return v;
	}

	@Override
	public String toString() {
		return "Stylize/Crystallize...";
	}
	
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("EdgeThickness")))!=null)setEdgeThickness(ImageFilterUtil.toFloatValue(o,"EdgeThickness"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("FadeEdges")))!=null)setFadeEdges(ImageFilterUtil.toBooleanValue(o,"FadeEdges"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("EdgeColor")))!=null)setEdgeColor(ImageFilterUtil.toColorRGB(o,"EdgeColor"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Colormap")))!=null)setColormap(ImageFilterUtil.toColormap(o,"Colormap"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Turbulence")))!=null)setTurbulence(ImageFilterUtil.toFloatValue(o,"Turbulence"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Stretch")))!=null)setStretch(ImageFilterUtil.toFloatValue(o,"Stretch"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Angle")))!=null)setAngle(ImageFilterUtil.toFloatValue(o,"Angle"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("AngleCoefficient")))!=null)setAngleCoefficient(ImageFilterUtil.toFloatValue(o,"AngleCoefficient"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("GradientCoefficient")))!=null)setGradientCoefficient(ImageFilterUtil.toFloatValue(o,"GradientCoefficient"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("F1")))!=null)setF1(ImageFilterUtil.toFloatValue(o,"F1"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("F2")))!=null)setF2(ImageFilterUtil.toFloatValue(o,"F2"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("F3")))!=null)setF3(ImageFilterUtil.toFloatValue(o,"F3"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("F4")))!=null)setF4(ImageFilterUtil.toFloatValue(o,"F4"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Randomness")))!=null)setRandomness(ImageFilterUtil.toFloatValue(o,"Randomness"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("GridType")))!=null)setGridType(ImageFilterUtil.toString(o,"GridType"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("DistancePower")))!=null)setDistancePower(ImageFilterUtil.toFloatValue(o,"DistancePower"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Scale")))!=null)setScale(ImageFilterUtil.toFloatValue(o,"Scale"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+eng.getListUtil().toList(parameters.keys(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [EdgeThickness, FadeEdges, EdgeColor, Colormap, Amount, Turbulence, Stretch, Angle, Coefficient, AngleCoefficient, GradientCoefficient, F1, F2, F3, F4, Randomness, GridType, DistancePower, Scale]",null);
		}

		return filter(src, dst);
	}
}