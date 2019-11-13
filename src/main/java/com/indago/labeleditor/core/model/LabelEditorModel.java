package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.model.tagging.LabelEditorTagging;
import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Comparator;
import java.util.List;

public interface LabelEditorModel <L> {

	void init(ImgLabeling<L, IntType> labels);

	ImgLabeling<L, IntType> labels();

	void init(ImgLabeling<L, IntType> labeling, ImgPlus data);

	List<LabelEditorColorset> getVirtualChannels();

	LabelEditorTagging<L> tagging();

	LabelEditorTagColors colors();

	void setTagComparator(Comparator<Object> comparator);

	void setLabelComparator(Comparator<L> comparator);

	Comparator<Object> getTagComparator();
	Comparator<L> getLabelComparator();

	ImgPlus getData();
	void setData(ImgPlus data);
}
