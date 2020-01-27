package sc.fiji.labeleditor.plugin.renderers;

import net.imglib2.type.numeric.IntegerType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColor;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColorset;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.colors.LabelEditorValueColor;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorValueTag;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	protected int[] lut;
	boolean debug = false;
	protected LabelEditorModel<L> model;

	@Override
	public void init(LabelEditorModel<L> model) {
		this.model = model;
	}

	@Override
	public void updateOnTagChange(LabelEditorModel<L> model) {
		updateLUT(model.labeling().getMapping(), model.colors(), LabelEditorTargetComponent.FACE);
	}

	protected void updateLUT(LabelingMapping<L> mapping, LabelEditorTagColors tagColors, LabelEditorTargetComponent targetComponent) {

		if(lut == null || lut.length != model.labeling().getMapping().numSets()) {
			lut = new int[model.labeling().getMapping().numSets()];
		} else {
			Arrays.fill(lut, 0);
		}

		if(tagColors == null) return;

		for (int i = 0; i < lut.length; i++) {

			Set<L> labels = mapping.labelsAtIndex(i);

			if(labels.size() == 0) continue;

			lut[i] = getMixColor(tagColors, targetComponent, labels);

		}

		if(debug) {
			printLUT(mapping, lut);
		}
	}

	protected int getMixColor(LabelEditorTagColors tagColors, Object targetComponent, Set<L> labels) {
		List<L> sortedLabels = new ArrayList<>(labels);
		sortedLabels.sort(model.getLabelComparator());

		int[] labelColors = new int[sortedLabels.size()];
		for (int j = 0; j < labelColors.length; j++) {

			Set<Object> labelTags = model.tagging().getTags(sortedLabels.get(j));
			ArrayList<Object> sortedTags = new ArrayList<>(labelTags);
			sortedTags.sort(model.getTagComparator());
			sortedTags.add(LabelEditorTag.DEFAULT);

			labelColors[j] = mixColorsAdditive(sortedTags, tagColors, targetComponent);
		}

		return ColorMixingUtils.mixColorsOverlay(labelColors);
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
		Converter<? super IntegerType<?>, ARGBType> converter = (i, o) -> o.set(getLUT()[i.getInteger()]);
		return Converters.convert(model.labeling().getIndexImg(), converter,
				new ARGBType());
	}

	protected int[] getLUT() {
		return lut;
	}

	public static int mixColorsAdditive(List<Object> tags, LabelEditorTagColors tagColors, Object targetComponent) {

		int[] colors = getTagColors(tags, tagColors, targetComponent);
		return ColorMixingUtils.mixColorsAdditive(colors);
	}

	public static int mixColorsOverlay(List<Object> tags, LabelEditorTagColors tagColors, Object targetComponent) {

		int[] colors = getTagColors(tags, tagColors, targetComponent);
		return ColorMixingUtils.mixColorsOverlay(colors);
	}

	private static int[] getTagColors(List< Object > tags,
			LabelEditorTagColors tagColors, Object targetComponent)
	{
		int[] colors = new int[tags.size()];
		for (int i = 0; i < colors.length; i++) {
			Object tag = tags.get(i);
			int color = getColor(tagColors, targetComponent, tag);
			colors[i] = color;
		}
		return colors;
	}

	private static int getColor(LabelEditorTagColors tagColors, Object targetComponent, Object tag) {
		int color = 0;
		if(LabelEditorValueTag.class.isAssignableFrom(tag.getClass())){
			LabelEditorColorset colorset = tagColors.getColorset(((LabelEditorValueTag) tag).getIdentifier());
			LabelEditorColor targetColor = colorset.get(targetComponent);
			if(targetColor == null) return 0;
			if(LabelEditorValueColor.class.isAssignableFrom(targetColor.getClass())) {
				RealType value = ((LabelEditorValueTag) tag).getValue();
				color = ((LabelEditorValueColor)targetColor).getColor(value);
			} else {
				color = targetColor.get();
			}
		} else {
			LabelEditorColorset colorset = tagColors.getColorset(tag);
			if(colorset != null && colorset.containsKey(targetComponent)) {
				color = colorset.get(targetComponent).get();
			}
		}
		return color;
	}

	void printLUT() {
		printLUT(model.labeling().getMapping(), lut);
	}
}
