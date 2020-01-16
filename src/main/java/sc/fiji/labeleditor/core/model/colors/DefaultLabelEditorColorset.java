package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.RealType;

import java.util.HashMap;

public class DefaultLabelEditorColorset extends HashMap<Object, LabelEditorColor> implements LabelEditorColorset {

	private final LabelEditorTagColors colors;

	public DefaultLabelEditorColorset(LabelEditorTagColors colors) {
		this.colors = colors;
	}

	@Override
	public LabelEditorColor put(Object o, int color) {
		LabelEditorColor put = super.put(o, new LabelEditorColor(this, color));
		update();
		return put;
	}

	@Override
	public LabelEditorColor put(Object o, LabelEditorColor color) {
		LabelEditorColor put = super.put(o, color);
		update();
		return put;
	}

	@Override
	public <T extends RealType<T>> LabelEditorColor put(Object o, int minColor, int maxColor, T min, T max) {
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(this, min, max);
		color.setMinColor(minColor).setMaxColor(maxColor);
		LabelEditorColor put = super.put(o, color);
		update();
		return put;
	}

	@Override
	public void update() {
		if(colors != null) colors.notifyListeners();
	}

	@Override
	public LabelEditorColor remove(Object o) {
		LabelEditorColor remove = super.remove(o);
		update();
		return remove;
	}

	@Override
	public void clear() {
		super.clear();
		update();
	}

	@Override
	public boolean remove(Object o, Object o1) {
		boolean remove = super.remove(o, o1);
		update();
		return remove;
	}

	@Override
	public LabelEditorColor get(Object o) {
		return computeIfAbsent(o, k -> new LabelEditorColor(this, 0));
	}

}
