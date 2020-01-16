package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.RealType;
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

public interface LabelEditorTagColors {
	LabelEditorColorset getColorset(Object tag);

	Listeners<ColorChangeListener> listeners();

	void pauseListeners();

	void resumeListeners();

	void notifyListeners();

	default LabelEditorColor getFaceColor(Object tag) {
		return getColorset(tag).get(LabelEditorTargetComponent.FACE);
	}

	default LabelEditorColor getBorderColor(Object tag) {
		return getColorset(tag).get(LabelEditorTargetComponent.BORDER);
	}

	default LabelEditorColor getFocusFaceColor() {
		return getFaceColor(LabelEditorTag.FOCUS);
	}

	default LabelEditorColor getSelectedFaceColor() {
		return getFaceColor(LabelEditorTag.SELECTED);
	}

	default LabelEditorColor getDefaultFaceColor() {
		return getFaceColor(LabelEditorTag.DEFAULT);
	}

	default LabelEditorColor getFocusBorderColor() {
		return getBorderColor(LabelEditorTag.FOCUS);
	}

	default LabelEditorColor getSelectedBorderColor() {
		return getBorderColor(LabelEditorTag.SELECTED);
	}

	default LabelEditorColor getDefaultBorderColor() {
		return getBorderColor(LabelEditorTag.DEFAULT);
	}

	default <T extends RealType<T>> LabelEditorValueColor<T> makeValueBorderColor(Object valueTagIdentifier) {
		LabelEditorColorset colorset = getColorset(valueTagIdentifier);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset);
		colorset.put(LabelEditorTargetComponent.BORDER, color);
		return color;
	}

	default <T extends RealType<T>> LabelEditorValueColor<T> makeValueFaceColor(Object valueTagIdentifier, T minVal, T maxVal) {
		LabelEditorColorset colorset = getColorset(valueTagIdentifier);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset, minVal, maxVal);
		colorset.put(LabelEditorTargetComponent.FACE, color);
		return color;

	}
}
