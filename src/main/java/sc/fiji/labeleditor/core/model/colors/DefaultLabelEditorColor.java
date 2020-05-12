package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.ARGBType;

import java.util.function.Function;

public class DefaultLabelEditorColor implements LabelEditorColor {
	private Function<Object, Integer> colorMapping;
	private final LabelEditorColorset colorset;

	public DefaultLabelEditorColor(LabelEditorColorset colorset) {
		this.colorset = colorset;
	}

	public DefaultLabelEditorColor(LabelEditorColorset colorset, int color) {
		this.colorset = colorset;
		this.colorMapping = v -> color;
	}

	@Override
	public int get() {
		return colorMapping.apply(true);
	}

	@Override
	public int get(Object value) {
		return colorMapping.apply(value);
	}

	@Override
	public void set(int color) {
		this.colorMapping = v -> color;
		update();
	}

	protected void update() {
		colorset.update();
	}

	@Override
	public void set(int red, int green, int blue, int alpha) {
		set(ARGBType.rgba(red, green, blue, alpha));
	}

	@Override
	public void set(int red, int green, int blue) {
		set(red, green, blue, 255);
	}
}
