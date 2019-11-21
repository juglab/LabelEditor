package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;

public class LabelEditorColor {
	private final LabelEditorColorset colorset;
	int color;

	public LabelEditorColor(LabelEditorColorset colorset, Integer color) {
		this.colorset = colorset;
		set(color);
	}

	public int get() {
		return color;
	}

	public void set(int color) {
		this.color = color;
		colorset.update();
	}

	public void set(int red, int green, int blue, int alpha) {
		set(ARGBType.rgba(red, green, blue, alpha));
	}

	public void set(int red, int green, int blue) {
		set(red, green, blue, 255);
	}
}
