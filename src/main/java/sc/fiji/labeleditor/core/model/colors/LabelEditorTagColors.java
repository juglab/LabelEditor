package sc.fiji.labeleditor.core.model.colors;

import net.imglib2.type.numeric.RealType;
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

import java.util.Collection;
import java.util.HashMap;

public class LabelEditorTagColors extends HashMap<Object, LabelEditorColorset> {

	private final Listeners.List<ColorChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public LabelEditorTagColors() {
	}

	public LabelEditorColorset getColorset(Object tag) {
		return computeIfAbsent(tag, k -> new LabelEditorColorset(this));
	}

	// listeners

	public Listeners<ColorChangeListener> listeners() {
		return listeners;
	}

	public void pauseListeners() {
		listenersPaused = true;
	}

	public void resumeListeners() {
		listenersPaused = false;
	}

	public void notifyListeners() {
		ColorChangedEvent e = new ColorChangedEvent();
		listeners.list.forEach(listener -> listener.tagChanged(e));
	}

	// convenience methods

	public LabelEditorColor getFaceColor(Object tag) {
		return getColorset(tag).get(LabelEditorTargetComponent.FACE);
	}

	public LabelEditorColor getBorderColor(Object tag) {
		return getColorset(tag).get(LabelEditorTargetComponent.BORDER);
	}

	public LabelEditorColor getFocusFaceColor() {
		return getFaceColor(LabelEditorTag.FOCUS);
	}

	public LabelEditorColor getSelectedFaceColor() {
		return getFaceColor(LabelEditorTag.SELECTED);
	}

	public LabelEditorColor getDefaultFaceColor() {
		return getFaceColor(LabelEditorTag.DEFAULT);
	}

	public LabelEditorColor getFocusBorderColor() {
		return getBorderColor(LabelEditorTag.FOCUS);
	}

	public LabelEditorColor getSelectedBorderColor() {
		return getBorderColor(LabelEditorTag.SELECTED);
	}

	public LabelEditorColor getDefaultBorderColor() {
		return getBorderColor(LabelEditorTag.DEFAULT);
	}

	public<T extends RealType<T>> LabelEditorValueColor<T> makeValueBorderColor(Object valueTagIdentifier) {
		LabelEditorColorset colorset = getColorset(valueTagIdentifier);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset);
		colorset.put(LabelEditorTargetComponent.BORDER, color);
		return color;
	}

	public <T extends RealType<T>> LabelEditorValueColor<T> makeValueFaceColor(Object valueTagIdentifier, T minVal, T maxVal) {
		LabelEditorColorset colorset = getColorset(valueTagIdentifier);
		LabelEditorValueColor<T> color = new LabelEditorValueColor<>(colorset, minVal, maxVal);
		colorset.put(LabelEditorTargetComponent.FACE, color);
		return color;

	}

	public Collection<LabelEditorColorset> getVirtualChannels() {
		return values();
	}
}
