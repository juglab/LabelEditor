package com.indago.labeleditor.display;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	private int[] lut;

	public DefaultLabelEditorRenderer() {}

	@Override
	public synchronized void update(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, Map<Object, LUTChannel> tagColors) {

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[mapping.numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<L> labels = mapping.labelsAtIndex(i);

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<Object> mytags = filterTagsByLabels( tags, labels);

			lut[i] = mixColors(mytags, tagColors);

		}

		this.lut = lut;
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getRenderedLabels(ImgLabeling<L, IntType> labels) {
//		update();
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(labels.getIndexImg(), converter, new ARGBType() );
	}

	public int[] getLUT() {
		return lut;
	}

	//https://en.wikipedia.org/wiki/Alpha_compositing
	//https://wikimedia.org/api/rest_v1/media/math/render/svg/12ea004023a1756851fc7caa0351416d2ba03bae
	public static int mixColors(Set<Object> mytags, Map<Object, LUTChannel> tagColors) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float alpha = 0;
		for (Object tag : mytags) {
			LUTChannel lutChannel = tagColors.get(tag);
			if(lutChannel == null) continue;
			int color = lutChannel.getColor();
			float newred = ARGBType.red(color);
			float newgreen = ARGBType.blue(color);
			float newblue = ARGBType.green(color);
			float newalpha = ((float)ARGBType.alpha(color))/255.f;
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			red = (red*alpha+newred*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			green = (green*alpha+newgreen*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			blue = (blue*alpha+newblue*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			alpha = alpha + newalpha*(1-alpha);
		}
		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
	}

	private synchronized Set<Object> filterTagsByLabels(Map<L, Set<Object>> tags, Set<L> labels) {
		Set<Object> set = new HashSet<>();
		for (Map.Entry<L, Set<Object>> entry : tags.entrySet()) {
			if (labels.contains(entry.getKey())) {
				set.addAll(entry.getValue());
			}
		}
		return set;
	}

}
