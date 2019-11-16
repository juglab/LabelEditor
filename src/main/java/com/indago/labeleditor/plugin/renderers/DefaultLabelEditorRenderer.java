package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.colors.LabelEditorColor;
import com.indago.labeleditor.core.model.colors.LabelEditorValueColor;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.tagging.LabelEditorValueTag;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "labels", priority = 2)
public class DefaultLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	protected int[] lut;
	boolean debug = false;
	protected LabelEditorModel model;

	@Override
	public void init(LabelEditorModel model) {
		this.model = model;
	}

	@Override
	public void updateOnTagChange(LabelEditorModel model) {
		updateLUT(model.labels().getMapping(), model.colors(), LabelEditorTargetComponent.FACE);
	}

	protected void updateLUT(LabelingMapping<L> mapping, LabelEditorTagColors tagColors, LabelEditorTargetComponent targetComponent) {
		int[] lut;

		lut = new int[mapping.numSets()];

		if(tagColors != null) {
			for (int i = 0; i < lut.length; i++) {

				Set<L> labels = mapping.labelsAtIndex(i);

				if(labels.size() == 0) continue;


				List<L> sortedLabels = new ArrayList<>(labels);
				sortedLabels.sort(model.getLabelComparator());

				int[] labelColors = new int[sortedLabels.size()];
				for (int j = 0; j < labelColors.length; j++) {

					Set labelTags = model.tagging().getTags(sortedLabels.get(j));
					ArrayList<Object> sortedTags = new ArrayList<>(labelTags);
					sortedTags.sort(model.getTagComparator());
					sortedTags.add(0, LabelEditorTag.DEFAULT);
					Collections.reverse(sortedTags);

					labelColors[j] = mixColors(sortedTags, tagColors, targetComponent);
				}

				lut[i] = mixColors(labelColors);

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
		return Converters.convert(model.labels().getIndexImg(), converter, new ARGBType() );
	}

	protected int[] getLUT() {
		return lut;
	}

	//https://en.wikipedia.org/wiki/Alpha_compositing
	//https://wikimedia.org/api/rest_v1/media/math/render/svg/12ea004023a1756851fc7caa0351416d2ba03bae
	public static int mixColors(List<Object> tags, LabelEditorTagColors tagColors, Object targetComponent) {

		int[] colors = new int[tags.size()];
		for (int i = 0; i < colors.length; i++) {
			Object tag = tags.get(i);
			int color = getColor(tagColors, targetComponent, tag);
			colors[i] = color;
		}
		return mixColors(colors);
	}

	private static int getColor(LabelEditorTagColors tagColors, Object targetComponent, Object tag) {
		int color = 0;
		if(tag.getClass().isAssignableFrom(LabelEditorValueTag.class)){
			LabelEditorColorset colorset = tagColors.get(((LabelEditorValueTag) tag).getIdentifier());
			LabelEditorColor targetColor = colorset.get(targetComponent);
			if(targetColor == null) return 0;
			if(targetColor.getClass().isAssignableFrom(LabelEditorValueColor.class)) {
				RealType value = ((LabelEditorValueTag) tag).getValue();
				color = ((LabelEditorValueColor)targetColor).getColor(value);
			} else {
				color = targetColor.getColor();
			}
		} else {
			LabelEditorColorset colorset = tagColors.get(tag);
			if(colorset != null && colorset.containsKey(targetComponent)) {
				color = colorset.get(targetComponent).getColor();
			}
		}
		return color;
	}

	public static int mixColors(int[] colors) {
		float red = 0;
		float green = 0;
		float blue = 0;
		float alpha = 0;
		for (int color : colors) {
			if(color == 0) continue;
			float newred = ARGBType.red(color);
			float newgreen = ARGBType.green(color);
			float newblue = ARGBType.blue(color);
			float newalpha = ((float)ARGBType.alpha(color))/255.f;
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			red = (red*alpha+newred*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			green = (green*alpha+newgreen*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			blue = (blue*alpha+newblue*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			alpha = alpha + newalpha*(1-alpha);
		}
		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
	}

	void printLUT() {
		printLUT(model.labels().getMapping(), lut);
	}
}
