package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorModel<V, T> implements LabelEditorModel<T> {

	private ImgLabeling<T, IntType > labels;
	private Map<T, Set<Object>> tags;

	public DefaultLabelEditorModel() {
		labels = null;
		tags = new HashMap<>();
	}

	public DefaultLabelEditorModel(List<ImgLabeling<T, IntType>> labels) {
		this((ImgLabeling<T, IntType>) Views.stack(labels));
	}

	public DefaultLabelEditorModel(ImgLabeling<T, IntType> labels) {
		this.labels = labels;
		tags = new HashMap<>();
	}

	@Override
	public ImgLabeling<T, IntType> getLabels() {
		return labels;
	}

	@Override
	public void setLabels(ImgLabeling<T, IntType> labels) {
		this.labels = labels;
	}

	@Override
	public Map<T, Set<Object>> getTags() {
		return tags;
	}

	@Override
	public void addTag(T label, Object tag) {
		if(tags == null) return;
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.add(tag);
	}

	@Override
	public void removeTag(T label, Object tag) {
		if(tags == null) return;
		Set<Object> set = tags.computeIfAbsent(label, k -> new HashSet<>());
		set.remove(tag);
	}

	@Override
	public Set<Object> getTags(T label) {
		if(tags == null) return null;
		return tags.computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public void removeTag(Object tag) {
		if(tags == null) return;
		tags.forEach( (label, tags) -> tags.remove(tag));
	}

}
