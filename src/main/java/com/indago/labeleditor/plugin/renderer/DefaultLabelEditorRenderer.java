package com.indago.labeleditor.plugin.renderer;

import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LUTChannel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "labels")
public class DefaultLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	protected int[] lut;
	protected ImgLabeling<L, IntType> labels;
	boolean debug = false;

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		this.labels = labels;
	}

	@Override
	public void updateOnTagChange(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, Map<Object, LUTChannel> tagColors) {

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[mapping.numSets()];

		if(tagColors != null) {
			for (int i = 0; i < lut.length; i++) {
				// get all labels of this index
				Set<L> labels = mapping.labelsAtIndex(i);

				// if there are no labels, we don't need to check for tags and can continue
				if(labels.size() == 0) continue;

				// get all tags associated with the labels of this index
				Set<Object> mytags = filterTagsByLabels( tags, labels);

				//add DEFAULT tag if no tag is assigned (making it possible to draw all labels with a default color)
				if(mytags.size() == 0) mytags.add(LabelEditorTag.NO_TAG);

				lut[i] = mixColors(mytags, tagColors);

			}
		}

		this.lut = lut;

		if(debug) {
			printLUT(mapping, lut);
		}
	}

	private void printLUT(LabelingMapping<L> mapping, int[] lut) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < lut.length; i++) {
			str.append("{");
			mapping.labelsAtIndex(i).forEach(label -> str.append(label).append(" "));
			str.append("} : rgba(");
			str.append(ARGBType.red(lut[i]));
			str.append(", ");
			str.append(ARGBType.green(lut[i]));
			str.append(", ");
			str.append(ARGBType.blue(lut[i]));
			str.append(", ");
			str.append(ARGBType.alpha(lut[i]));
			str.append(")\n");
		}
		System.out.println(str.toString());
	}

	@Override
	public void updateOnLabelingChange() {

	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(labels.getIndexImg(), converter, new ARGBType() );
	}

	protected int[] getLUT() {
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

	protected synchronized Set<Object> filterTagsByLabels(Map<L, Set<Object>> tags, Set<L> labels) {
		Set<Object> set = new HashSet<>();
		if(tags == null) return set;
		for (Map.Entry<L, Set<Object>> entry : tags.entrySet()) {
			if (labels.contains(entry.getKey())) {
				set.addAll(entry.getValue());
			}
		}
		return set;
	}

}
