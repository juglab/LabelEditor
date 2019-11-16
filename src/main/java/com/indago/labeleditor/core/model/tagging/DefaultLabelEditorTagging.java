package com.indago.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorTagging<L> implements LabelEditorTagging<L> {

	@Parameter
	LogService log;

	private final HashMap<L, Set<Object>> tags = new HashMap<>();
	private final Listeners.List<TagChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private List<TagChangedEvent> keptEvents = new ArrayList<>();

	@Override
	public Listeners< TagChangeListener > listeners() {
		return listeners;
	}

	@Override
	public void pauseListeners() {
		listenersPaused = true;
	}

	@Override
	public void resumeListeners() {
		listenersPaused = false;
		if(keptEvents.size() > 0) {
			listeners.list.forEach(listener -> listener.tagChanged(keptEvents));
			keptEvents.clear();
		}
	}

	@Override
	public Set<Object> getAllTags() {
		Set<Object> res = new HashSet<>();
		tags.forEach((label, tags) -> res.addAll(tags));
		return res;
	}

	private void notifyListeners(Object tag, L label, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.label = label;
		if(log!= null) log.debug(e.toString());
		if(listenersPaused) {
			keptEvents.add(e);
		} else {
			listeners.list.forEach(listener -> listener.tagChanged(Collections.singletonList(e)));
		}
	}

	public Map<L, Set<Object>> get() {
		return Collections.unmodifiableMap(tags);
	}

	@Override
	public void addTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		if(set.add(tag)) {
			notifyListeners(tag, label, TagChangedEvent.Action.ADDED);
		}
	}

	@Override
	public void removeTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		if( set.removeIf(mytag -> mytag.equals(tag))) {
			notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
		}
	}

	@Override
	public Set<Object> getTags(L label) {
		return tags.computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public synchronized void removeTag(Object tag) {
		tags.forEach( (label, tags) -> {
			if( tags.removeIf(mytag -> mytag.equals(tag))) {
				notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
			}
		});

	}

	@Override
	public Set<L> getLabels(Object tag) {
		Set<L> labels = new HashSet<>();
		tags.forEach((l, tags) -> {
			if(tags.contains(tag)) labels.add(l);
		});
		return labels;
	}

}
