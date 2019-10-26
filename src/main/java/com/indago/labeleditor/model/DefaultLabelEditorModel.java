package com.indago.labeleditor.model;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorModel<L> implements LabelEditorModel<L> {

	private ImgLabeling<L, IntType > labels;
	private final Map<L, Set<Object>> tags = new HashMap<>();
	private Map<L, LabelRegion<L>> orderedLabels;
	private final List<TagChangeListener> listeners = new ArrayList<>();

	public DefaultLabelEditorModel() {
		labels = null;
	}

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labels) {
		setLabels(labels);
	}

	@Override
	public ImgLabeling<L, IntType> getLabels() {
		return labels;
	}

	@Override
	public void setLabels(ImgLabeling<L, IntType> labeling) {
		this.labels = labeling;
		createOrderedLabels(labeling);
	}

	private void createOrderedLabels(ImgLabeling<L, IntType> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		List<LabelRegion<L>> regionSet = new ArrayList<>();
		labeling.forEach(labels -> labels.stream().map(regions::getLabelRegion).forEach(labelRegion -> {
			if (!regionSet.contains(labelRegion)) regionSet.add(labelRegion);
		}));
		//TODO check if this is correct - sort by region size?! probably does not work this way
		regionSet.sort((r1, r2) -> (int)(r1.size() - r2.size()));
		orderedLabels = new LinkedHashMap<>();
		regionSet.forEach(region -> orderedLabels.put(region.getLabel(), region));
	}

	@Override
	public Map<L, Set<Object>> getTags() {
		return tags;
	}

	@Override
	public void addTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.add(tag);
		notifyListeners(tag, label, TagChangedEvent.Action.ADDED);
	}

	@Override
	public void removeTag(Object tag, L label) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.remove(tag);
		notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
	}

	@Override
	public Set<Object> getTags(L label) {
		return tags.computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public void removeTag(Object tag) {
		tags.forEach( (label, tags) -> {
			tags.remove(tag);
			notifyListeners(tag, label, TagChangedEvent.Action.REMOVED);
		});
	}

	private void notifyListeners(Object tag, L label, TagChangedEvent.Action action) {
		TagChangedEvent e = new TagChangedEvent();
		e.action = action;
		e.tag = tag;
		e.label = label;
		listeners.forEach(listener -> listener.tagChanged(e));
	}

	@Override
	public int compare(L label1, L label2) {
		for (Map.Entry<L, LabelRegion<L>> entry : orderedLabels.entrySet()) {
			if(entry.getKey().equals(label1)) return 1;
			if(entry.getKey().equals(label2)) return -1;
		}
		return 0;
	}

	@Override
	public void addListener(TagChangeListener listener) {
		listeners.add(listener);
	}

	public Map<L, LabelRegion <L> > getOrderedLabelRegions() {
		return orderedLabels;
	}

}
