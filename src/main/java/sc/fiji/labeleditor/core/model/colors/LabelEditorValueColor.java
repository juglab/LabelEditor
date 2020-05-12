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
