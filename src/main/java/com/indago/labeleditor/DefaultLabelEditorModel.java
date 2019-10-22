package com.indago.labeleditor;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.List;

public class DefaultLabelEditorModel<V extends RealType<V> & NativeType<V>, T> implements LabelEditorModel {

	private final ImgPlus<V> data;
	protected ImgLabeling<T, IntType> labelingFrames;
	private List< ImgLabeling<T, IntType > > labels;

	public DefaultLabelEditorModel(ImgPlus<V> data, List<ImgLabeling<T, IntType>> labels) {
		this.data = data;
		this.labels = labels;
	}

	@Override
	public ImgLabeling<T, IntType> getLabels(int time) {
		return labels.get(time);
	}

	@Override
	public ImgPlus getData() {
		return data;
	}

	@Override
	public void setTag(Object label, LabelEditorTag tag) {

	}

}
