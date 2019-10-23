package com.indago.labeleditor.model;

import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Map;
import java.util.Set;

public interface LabelEditorModel <T>  {
	ImgPlus getData();

	ImgLabeling<T, IntType> getLabels(int time);

	Map< T, Set <LabelEditorTag> > getTags(int time);

	void addTag(int time, T label, LabelEditorTag tag);

	void removeTag(int time, T label, LabelEditorTag tag);

	Set<LabelEditorTag> getTags(int time, T label);
}
