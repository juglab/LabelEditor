package com.indago.labeleditor.display;

import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultLUTBuilder<U> implements LUTBuilder<U> {

	private static int colorBG = ARGBType.rgba(0,0,255,255);
	private static int colorLabeled = ARGBType.rgba(255,0,0,255);
	private static int colorSelected = ARGBType.rgba(0,255,0,255);

	public int[] build(ImgLabeling<U, IntType> img, Map<U, Set<LabelEditorTag>> tags) {

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[img.getMapping().numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<U> labels = img.getMapping().labelsAtIndex(i);

			// distinguish between background index and labeled indices
			lut[i] = labels.size() > 0 ? colorLabeled : colorBG;

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<LabelEditorTag> mytags = filterTagsByLabels(tags, labels);

			// set the color depending on the existence of specific tags
			if(mytags.contains(LabelEditorTag.SELECTED)) {
				lut[i] = colorSelected;
			}
		}

		return lut;
	}

	private Set<LabelEditorTag> filterTagsByLabels(Map<U, Set<LabelEditorTag>> tags, Set<U> labels) {
		return tags.entrySet().stream().filter(entry -> labels.contains(entry.getKey())).map(Map.Entry::getValue).flatMap(Set::stream).collect(Collectors.toSet());
	}

}
