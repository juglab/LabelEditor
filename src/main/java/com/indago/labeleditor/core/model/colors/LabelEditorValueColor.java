package com.indago.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;

public class LabelEditorValueColor<T extends RealType<T>> extends LabelEditorColor {
	private final int minColor;
	private final int maxColor;
	private final T minVal;
	private final T maxVal;

	public LabelEditorValueColor(LabelEditorColorset colorset, int minColor, int maxColor, T min, T max) {
		super(colorset, maxColor);
		this.minColor = minColor;
		this.maxColor = maxColor;
		this.minVal = min;
		this.maxVal = max;
	}

	public int getColor(RealType value) {
		float minR = ARGBType.red(minColor);
		float minG = ARGBType.green(minColor);
		float minB = ARGBType.blue(minColor);
		float minA = ARGBType.alpha(minColor);
		float maxR = ARGBType.red(maxColor);
		float maxG = ARGBType.green(maxColor);
		float maxB = ARGBType.blue(maxColor);
		float maxA = ARGBType.alpha(maxColor);
		float pct = (value.getRealFloat()-minVal.getRealFloat())/(maxVal.getRealFloat()-minVal.getRealFloat());
		int r = (int) (minR + (maxR-minR)*pct);
		int g = (int) (minG + (maxG-minG)*pct);
		int b = (int) (minB + (maxB-minB)*pct);
		int a = (int) (minA + (maxA-minA)*pct);
		return ARGBType.rgba(r,g,b,a);
	}
}
