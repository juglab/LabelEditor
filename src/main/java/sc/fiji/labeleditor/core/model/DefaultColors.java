package sc.fiji.labeleditor.core.model;

import net.imglib2.type.numeric.ARGBType;

public class DefaultColors {
	public static int selectedFace() { return ARGBType.rgba(0,100,255,200); }
	public static int selectedBorder() { return ARGBType.rgba(0,80,225,200); }
	public static int defaultFace() { return ARGBType.rgba(42,42,42,50); }
	public static int defaultBorder() { return ARGBType.rgba(255,255,255,100); }
	public static int focusBorder() { return ARGBType.rgba(200,200,200,200); }
	public static int focusFace() { return ARGBType.rgba(200,200,200,200); }
}
