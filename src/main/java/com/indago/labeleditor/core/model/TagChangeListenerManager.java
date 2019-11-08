package com.indago.labeleditor.core.model;

import java.util.ArrayList;
import java.util.List;

//TODO use scijava listeners
public class TagChangeListenerManager {

	private final List<TagChangeListener> listeners = new ArrayList<>();

	void addListener(TagChangeListener listener) {
		listeners.add(listener);
	}

	public <L> void notifyListeners(Object tag, L label, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.label = label;
		//TODO use scijava (?) to print in debug mode
		System.out.println("[INFO] " + e.toString());
		listeners.forEach(listener -> listener.tagChanged(e));
	}
}
