package com.indago.labeleditor.model;

import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.awt.event.ActionListener;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LabelEditorModel <L>  {

	ImgLabeling<L, IntType> getLabels();

	void setLabels(ImgLabeling<L, IntType> labels);

	Map<L, Set <Object> > getTags();

	void addTag(Object tag, L label);

	void removeTag(Object tag, L label);

	Set<Object> getTags(L label);

	void removeTag(Object tag);

	int compare(L label1, L label2);

	void addListener(TagChangeListener listener);

	List<L> getLabels(LabelEditorTag tag);
}
