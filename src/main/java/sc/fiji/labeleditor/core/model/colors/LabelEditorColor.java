package sc.fiji.labeleditor.core.model.colors;

public interface LabelEditorColor {
	int get();
	int get(Object value);
	void set(int color);

	void set(int red, int green, int blue, int alpha);

	void set(int red, int green, int blue);
}
