package com.indago.labeleditor.model;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Map;
import java.util.Set;

public interface LabelEditorModel <T>  {

	ImgLabeling<T, IntType> getLabels();

	void setLabels(ImgLabeling<T, IntType> labels);

	Map< T, Set <Object> > getTags();

	void addTag(T label, Object tag);

	void removeTag(T label, Object tag);

	Set<Object> getTags(T label);

	void removeTag(Object tag);

	int compare(T label1, T label2);
}
