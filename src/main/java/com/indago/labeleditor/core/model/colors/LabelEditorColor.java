package com.indago.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;

public class LabelEditorColor {
	int color;

	public LabelEditorColor(Integer color) {
		set(color);
	}

	public int get() {
		return color;
	}

	public void set(int color) {
		this.color = color;
	}

	public void set(int red, int green, int blue, int alpha) {
		this.color = ARGBType.rgba(red, green, blue, alpha);
	}

	public void set(int red, int green, int blue) {
		this.color = ARGBType.rgba(red, green, blue, 255);
	}
}
