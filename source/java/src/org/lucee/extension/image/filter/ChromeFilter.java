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
 * A filter which simulates chrome.
 */
public class ChromeFilter extends LightFilter  implements DynFiltering {
	private float amount = 0.5f;
	private float exposure = 1.0f;

	/**
	 * Set the amount of effect.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
	public void setAmount(float amount) {
		this.amount = amount;
	}

	/**
	 * Get the amount of chrome.
	 * @return the amount
     * @see #setAmount
	 */
	public float getAmount() {
		return amount;
	}

	/**
	 * Set the exppsure of the effect.
	 * @param exposure the exposure
     * @min-value 0
     * @max-value 1
     * @see #getExposure
	 */
	public void setExposure(float exposure) {
		this.exposure = exposure;
	}
	
	/**
	 * Get the exppsure of the effect.
	 * @return the exposure
     * @see #setExposure
	 */
	public float getExposure() {
		return exposure;
	}

    @Override
	public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
		setColorSource( LightFilter.COLORS_CONSTANT );
		dst = super.filter( src, dst );
		TransferFilter tf = new TransferFilter() {
			@Override
			protected float transferFunction( float v ) {
				v += amount * (float)Math.sin( v * 2 * Math.PI );
				return 1 - (float)Math.exp(-v * exposure);
			}
		};
        return tf.filter( dst, dst );
    }

	@Override
	public String toString() {
		return "Effects/Chrome...";
	}
	@Override
	public BufferedImage filter(BufferedImage src, Struct parameters) throws PageException {BufferedImage dst=ImageUtil.createBufferedImage(src);
		Object o;
		CFMLEngine eng = CFMLEngineFactory.getInstance();
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Amount")))!=null)setAmount(ImageFilterUtil.toFloatValue(o,"Amount"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Exposure")))!=null)setExposure(ImageFilterUtil.toFloatValue(o,"Exposure"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("ColorSource")))!=null)setColorSource(ImageFilterUtil.toColorRGB(o,"ColorSource"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("Material")))!=null)setMaterial(ImageFilterUtil.toLightFilter$Material(o,"Material"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("BumpFunction")))!=null)setBumpFunction(ImageFilterUtil.toFunction2D(o,"BumpFunction"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("BumpHeight")))!=null)setBumpHeight(ImageFilterUtil.toFloatValue(o,"BumpHeight"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("BumpSoftness")))!=null)setBumpSoftness(ImageFilterUtil.toFloatValue(o,"BumpSoftness"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("BumpShape")))!=null)setBumpShape(ImageFilterUtil.toIntValue(o,"BumpShape"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("ViewDistance")))!=null)setViewDistance(ImageFilterUtil.toFloatValue(o,"ViewDistance"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("EnvironmentMap")))!=null)setEnvironmentMap(ImageFilterUtil.toBufferedImage(o,"EnvironmentMap"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("BumpSource")))!=null)setBumpSource(ImageFilterUtil.toIntValue(o,"BumpSource"));
		if((o=parameters.removeEL(eng.getCreationUtil().createKey("DiffuseColor")))!=null)setDiffuseColor(ImageFilterUtil.toColorRGB(o,"DiffuseColor"));

		// check for arguments not supported
		if(parameters.size()>0) {
			throw eng.getExceptionUtil().createFunctionException(eng.getThreadPageContext(), "ImageFilter", 3, "parameters", "the parameter"+(parameters.size()>1?"s":"")+" ["+eng.getListUtil().toList(parameters.keys(),", ")+"] "+(parameters.size()>1?"are":"is")+" not allowed, only the following parameters are supported [Amount, Exposure, ColorSource, Material, BumpFunction, BumpHeight, BumpSoftness, BumpShape, ViewDistance, EnvironmentMap, BumpSource, DiffuseColor]",null);
		}

		return filter(src, dst);
	}
}