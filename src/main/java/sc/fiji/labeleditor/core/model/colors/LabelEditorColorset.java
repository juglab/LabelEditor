package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.RealType;

public interface LabelEditorColorset {
	LabelEditorColor put(Object targetKey, LabelEditorColor color);
	LabelEditorColor put(Object targetKey, int color);
	<T extends RealType<T>> LabelEditorColor put(Object targetKey, int minColor, int maxColor, T min, T max);
	LabelEditorColor remove(Object targetKey);
	void clear();
	void update();
	boolean containsKey(Object targetKey);
	LabelEditorColor get(Object targetKey);
}
