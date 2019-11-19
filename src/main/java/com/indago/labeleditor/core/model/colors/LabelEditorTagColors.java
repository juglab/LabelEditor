package com.indago.labeleditor.core.model.colors;

import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.type.numeric.ARGBType;
import org.scijava.listeners.Listeners;

import java.util.HashMap;

public class LabelEditorTagColors extends HashMap<Object, LabelEditorColorset> {

	private final Listeners.List<ColorChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public LabelEditorTagColors() {
	}

	public LabelEditorColorset get(Object tag) {
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

	void notifyListeners() {
		ColorChangedEvent e = new ColorChangedEvent();
		listeners.list.forEach(listener -> listener.tagChanged(e));
	}

	// convenience methods

	public void setFaceColor(Object tag, int r, int g, int b, int a) {
		get(tag).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(r, g, b, a));
	}

	public void setBorderColor(Object tag, int r, int g, int b, int a) {
		get(tag).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(r, g, b, a));
	}

	public void setMouseOverFaceColor(int r, int g, int b, int a) {
		setFaceColor(LabelEditorTag.MOUSE_OVER, r, g, b, a);
	}

	public void setSelectedFaceColor(int r, int g, int b, int a) {
		setFaceColor(LabelEditorTag.SELECTED, r, g, b, a);
	}

	public void setDefaultFaceColor(int r, int g, int b, int a) {
		setFaceColor(LabelEditorTag.DEFAULT, r, g, b, a);
	}

	public void setMouseOverBorderColor(int r, int g, int b, int a) {
		setBorderColor(LabelEditorTag.MOUSE_OVER, r, g, b, a);
	}

	public void setSelectedBorderColor(int r, int g, int b, int a) {
		setBorderColor(LabelEditorTag.SELECTED, r, g, b, a);
	}

	public void setDefaultBorderColor(int r, int g, int b, int a) {
		setBorderColor(LabelEditorTag.DEFAULT, r, g, b, a);
	}
}
