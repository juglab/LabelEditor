package com.indago.labeleditor.core.model.colors;

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
}
