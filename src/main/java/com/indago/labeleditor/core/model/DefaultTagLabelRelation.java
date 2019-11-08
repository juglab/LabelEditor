package com.indago.labeleditor.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultTagLabelRelation<L> implements TagLabelRelation<L> {

	private final TagChangeListenerManager listenerManager;
	private HashMap<L, Set<Object>> tags;

	public DefaultTagLabelRelation(TagChangeListenerManager listenerManager) {
		tags = new HashMap<>();
		this.listenerManager = listenerManager;
	}

	public Map<L, Set<Object>> get() {
		return Collections.unmodifiableMap(tags);
	}

	@Override
	public void addTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		if(set.contains(tag)) return;
		set.add(tag);
		listenerManager.notifyListeners(tag, label, TagChangedEvent.Action.ADDED);
	}

	@Override
	public void removeTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		if(!set.contains(tag)) return;
		set.remove(tag);
		listenerManager.notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
	}

	@Override
	public Set<Object> getTags(L label) {
		return tags.computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public void removeTag(Object tag) {
		tags.forEach( (label, tags) -> {
			if(!tags.contains(tag)) return;
			tags.remove(tag);
			listenerManager.notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
		});

	}

	@Override
	public List<L> getLabels(LabelEditorTag tag) {
		List<L> labels = new ArrayList<>();
		tags.forEach((l, tags) -> {
			if(tags.contains(tag)) labels.add(l);
		});
		return labels;
	}

}
