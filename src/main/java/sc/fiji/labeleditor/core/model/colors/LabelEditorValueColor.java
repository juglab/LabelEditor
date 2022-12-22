/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

import java.util.function.Function;

public class LabelEditorValueColor<T extends RealType<T>> implements LabelEditorColor {
	private int minColor;
	private int maxColor;
	private T minVal;
	private T maxVal;

	private Function<T, Integer> colorMapping;
	private final LabelEditorColorset colorset;

	public LabelEditorValueColor(LabelEditorColorset colorset) {
		this.colorset = colorset;
	}

	public LabelEditorValueColor(LabelEditorColorset colorset, T minVal, T maxVal) {
		this.colorset = colorset;
		this.minVal = minVal;
		this.maxVal = maxVal;
	}

	@Override
	public int get() {
		return 0;
	}

	@Override
	public int get(Object value) {
		if(colorMapping == null) return 0;
		return colorMapping.apply((T)value);
	}

	@Override
	public void set(int color) {

	}

	@Override
	public void set(int red, int green, int blue, int alpha) {

	}

	@Override
	public void set(int red, int green, int blue) {

	}

	protected void update() {
		colorMapping = value -> {
			float minR = ARGBType.red(minColor);
			float minG = ARGBType.green(minColor);
			float minB = ARGBType.blue(minColor);
			float minA = ARGBType.alpha(minColor);
			float maxR = ARGBType.red(maxColor);
			float maxG = ARGBType.green(maxColor);
			float maxB = ARGBType.blue(maxColor);
			float maxA = ARGBType.alpha(maxColor);
			float minFloat = minVal.getRealFloat();
			float maxFloat = maxVal.getRealFloat();
			float valueFloat = value.getRealFloat();
			if(valueFloat < minFloat) valueFloat = minFloat;
			if(valueFloat > maxFloat) valueFloat = maxFloat;
			float pct = (valueFloat - minFloat)/(maxFloat - minFloat);
			int r = (int) (minR + (maxR-minR)*pct);
			int g = (int) (minG + (maxG-minG)*pct);
			int b = (int) (minB + (maxB-minB)*pct);
			int a = (int) (minA + (maxA-minA)*pct);
			return ARGBType.rgba(r,g,b,a);
		};
		colorset.update();
	}

	public LabelEditorValueColor<T> setMinColor(int red, int green, int blue, int alpha) {
		minColor = ARGBType.rgba(red, green, blue, alpha);
		update();
		return this;
	}

	public LabelEditorValueColor<T> setMaxColor(int red, int green, int blue, int alpha) {
		maxColor = ARGBType.rgba(red, green, blue, alpha);
		update();
		return this;
	}

	public LabelEditorValueColor<T> setMinValue(T val) {
		minVal = val;
		update();
		return this;
	}

	public LabelEditorValueColor<T> setMaxValue(T val) {
		maxVal = val;
		update();
		return this;
	}

	public LabelEditorValueColor<T> setMinColor(int color) {
		minColor = color;
		update();
		return this;
	}

	public LabelEditorValueColor<T> setMaxColor(int color) {
		maxColor = color;
		update();
		return this;
	}
}
