package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorModel<V extends RealType<V> & NativeType<V>, T> implements LabelEditorModel<T> {

	private final ImgPlus<V> data;
	private final List< ImgLabeling<T, IntType > > labels;
	private final List <Map<T, Set<LabelEditorTag>>> tags;

	public DefaultLabelEditorModel(ImgPlus<V> data, List<ImgLabeling<T, IntType>> labels) {
		this.data = data;
		this.labels = labels;
		tags = new ArrayList<>();
	}

	@Override
	public ImgLabeling<T, IntType> getLabels(int time) {
		return labels.get(time);
	}

	@Override
	public Map<T, Set<LabelEditorTag>> getTags(int time) {
		if(tags.size() <= time) return null;
		return tags.get(time);
	}

	@Override
	public ImgPlus getData() {
		return data;
	}


}
