package com.indago.labeleditor.core.model.tagging;

import org.scijava.listeners.Listeners;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

	protected HashMap<L, Set<Object>> tagMap() {
		return tags;
	}

	@Override
	public Set<Object> getAllTags() {
		Set<Object> res = new HashSet<>();
		tagMap().forEach((label, tags) -> res.addAll(tags));
		return res;
	}

	@Override
	public Set<L> filterLabelsWithTag(Set<L> labels, Object tag) {
		return labels.stream()
				.filter(label -> tagMap().containsKey(label)
						&& tagMap().get(label).contains(tag))
				.collect(Collectors.toSet());
	}

	@Override
	public Set filterLabelsWithAnyTag(Set<L> labels, Set<Object> tags) {
		return labels.stream()
				.filter(label -> tagMap().containsKey(label)
						&& tagMap().get(label).stream().anyMatch(tags::contains))
				.collect(Collectors.toSet());
	}

	protected void notifyListeners(TagChangedEvent e) {
		if(log!= null) log.debug(e.toString());
		if(listenersPaused) {
			keptEvents.add(e);
		} else {
			listeners.list.forEach(listener -> listener.tagChanged(Collections.singletonList(e)));
		}
	}

	protected void notifyListeners(Object tag, L label, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.label = label;
		notifyListeners(e);
	}

	public Map<L, Set<Object>> get() {
		return Collections.unmodifiableMap(tagMap());
	}

	@Override
	public void addTag(Object tag, L label) {
		Set<Object> set = tagMap().computeIfAbsent(label, k -> new HashSet<>());
		if(set.add(tag)) {
			notifyListeners(tag, label, TagChangedEvent.Action.ADDED);
		}
	}

	@Override
	public void removeTag(Object tag, L label) {
		Set<Object> set = tagMap().get(label);
		if(set == null) return;
		if( set.removeIf(mytag -> mytag.equals(tag))) {
			notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
		}
	}

	@Override
	public Set<Object> getTags(L label) {
		return tagMap().computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public synchronized void removeTag(Object tag) {
		tagMap().forEach( (label, tags) -> {
			if( tags.removeIf(mytag -> mytag.equals(tag))) {
				notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
			}
		});

	}

	@Override
	public Set<L> getLabels(Object tag) {
		Set<L> labels = new HashSet<>();
		tagMap().forEach((l, tags) -> {
			if(tags.contains(tag)) labels.add(l);
		});
		return labels;
	}

}
