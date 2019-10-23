package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorModel<V, T> implements LabelEditorModel<T> {

	final ImgPlus<V> data;
	final List< ImgLabeling<T, IntType > > labels;
	final List <Map<T, Set<Object>>> tags;

	public DefaultLabelEditorModel(ImgPlus<V> data, ImgLabeling<T, IntType> labels) {
		this(data, Collections.singletonList(labels));
	}

	public DefaultLabelEditorModel(ImgPlus<V> data, List<ImgLabeling<T, IntType>> labels) {
		this.data = data;
		this.labels = labels;
		tags = new ArrayList<>(labels.size());
		for (int i = 0; i < labels.size(); i++) {
			tags.add(new HashMap<>());
		}
	}

	@Override
	public ImgLabeling<T, IntType> getLabels(int time) {
		return labels.get(time);
	}

	@Override
	public Map<T, Set<Object>> getTags(int time) {
		if(tags.size() <= time) return null;
		return tags.get(time);
	}

	@Override
	public void addTag(int time, T label, Object tag) {
		Set<Object> set = tags.get(time).computeIfAbsent(label, k -> new HashSet<>());
		set.add(tag);
	}

	@Override
	public void removeTag(int time, T label, Object tag) {
		Set<Object> set = tags.get(time).computeIfAbsent(label, k -> new HashSet<>());
		set.remove(tag);
	}

	@Override
	public Set<Object> getTags(int time, T label) {
		return tags.get(time).computeIfAbsent(label, k -> new HashSet<>());
	}

	@Override
	public ImgPlus getData() {
		return data;
	}


}
