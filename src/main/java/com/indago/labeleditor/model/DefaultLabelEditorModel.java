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

public class DefaultLabelEditorModel<V, T> implements LabelEditorModel<T> {

	private ImgLabeling<T, IntType > labels;
	private final Map<T, Set<Object>> tags = new HashMap<>();
	private Map<T, LabelRegion<T>> orderedLabels;


	public DefaultLabelEditorModel() {
		labels = null;
	}

	public DefaultLabelEditorModel(ImgLabeling<T, IntType> labels) {
		setLabels(labels);
	}

	@Override
	public ImgLabeling<T, IntType> getLabels() {
		return labels;
	}

	@Override
	public void setLabels(ImgLabeling<T, IntType> labeling) {
		this.labels = labeling;
		createOrderedLabels(labeling);
	}

	private void createOrderedLabels(ImgLabeling<T, IntType> labeling) {
		LabelRegions<T> regions = new LabelRegions<>(labeling);
		List<LabelRegion<T>> regionSet = new ArrayList<>();
		labeling.forEach(labels -> labels.stream().map(regions::getLabelRegion).forEach(labelRegion -> {
			if (!regionSet.contains(labelRegion)) regionSet.add(labelRegion);
		}));
		//TODO check if this is correct - sort by region size?! probably does not work this way
		regionSet.sort((r1, r2) -> (int)(r1.size() - r2.size()));
		orderedLabels = new LinkedHashMap<>();
		regionSet.forEach(region -> orderedLabels.put(region.getLabel(), region));
	}

	@Override
	public Map<T, Set<Object>> getTags() {
		return tags;
	}

	@Override
	public void addTag(T label, Object tag) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.add(tag);
	}

	@Override
	public void removeTag(T label, Object tag) {
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.remove(tag);
	}

	@Override
	public Set<Object> getTags(T label) {
		return tags.computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public void removeTag(Object tag) {
		tags.forEach( (label, tags) -> tags.remove(tag));
	}

	@Override
	public int compare(T label1, T label2) {
		for (Map.Entry<T, LabelRegion<T>> entry : orderedLabels.entrySet()) {
			if(entry.getKey().equals(label1)) return 1;
			if(entry.getKey().equals(label2)) return -1;
		}
		return 0;
	}

	public Map<T, LabelRegion <T> > getOrderedLabelRegions() {
		return orderedLabels;
	}

}
