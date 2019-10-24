package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorModel<V, T> implements LabelEditorModel<T> {

	final ImgPlus<V> data;
	final ImgLabeling<T, IntType > labels;
	final Map<T, Set<Object>> tags;

	public DefaultLabelEditorModel(ImgPlus<V> data, List<ImgLabeling<T, IntType>> labels) {
		this(data, (ImgLabeling<T, IntType>) Views.stack(labels));
	}

	public DefaultLabelEditorModel(ImgPlus<V> data, ImgLabeling<T, IntType> labels) {
		this.data = data;
		this.labels = labels;
		tags = new HashMap<>();
	}

	@Override
	public ImgLabeling<T, IntType> getLabels() {
		return labels;
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
	public ImgPlus getData() {
		return data;
	}


}
