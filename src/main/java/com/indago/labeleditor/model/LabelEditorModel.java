package com.indago.labeleditor.model;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Comparator;

public interface LabelEditorModel <L> {

	void init(ImgLabeling<L, IntType> labels);

	ImgLabeling<L, IntType> labels();

	TagLabelRelation<L> tagging();

	TagChangeListenerManager listener();

	void setTagComparator(Comparator<Object> comparator);

	void setLabelComparator(Comparator<L> comparator);

	Comparator<Object> getTagComparator();
	Comparator<L> getLabelComparator();
}
