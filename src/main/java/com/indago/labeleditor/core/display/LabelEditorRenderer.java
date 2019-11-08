package com.indago.labeleditor.core.display;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Map;
import java.util.Set;

public interface LabelEditorRenderer<L> {
	void update(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, Map<Object, LUTChannel> tagColors);
	RandomAccessibleInterval getRenderedLabels(ImgLabeling<L, IntType> labels);
	String getName();
}
