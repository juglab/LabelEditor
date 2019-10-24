package com.indago.labeleditor.display;

import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Map;
import java.util.Set;

public interface LUTBuilder<U> {
	/**
	 * @param img the labeling do display
	 * @param tags the tags corresponding to the labels in {@param img}
	 * @return a lookup table matching the indices of {@param img} to a specific {@link ARGBType} color
	 */
	int[] build(ImgLabeling<U, IntType> img, Map<U, Set<LabelEditorTag>> tags);
}
