package com.indago.labeleditor;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

public interface LabelEditorModel <T>  {
	ImgLabeling<T, IntType> getLabels(int time);

	ImgPlus getData();

	void setTag(T label, LabelEditorTag tag);
}
