package com.indago.labeleditor.core.model;

import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.model.tagging.LabelEditorTagging;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Comparator;
import java.util.List;

public interface LabelEditorModel <L> {

	void init(Img indexImg);

	void init(Img indexImg, Img data);

	void init(ImgLabeling<L, IntType> labeling);

	void init(ImgLabeling<L, IntType> labeling, Img data);

	ImgLabeling<L, IntType> labels();

	LabelEditorTagging<L> tagging();

	LabelEditorTagColors colors();

	List<LabelEditorColorset> getVirtualChannels();

	void setTagComparator(Comparator<Object> comparator);

	void setLabelComparator(Comparator<L> comparator);

	Comparator<Object> getTagComparator();
	Comparator<L> getLabelComparator();

	Img getData();
	void setData(Img data);
}
