package sc.fiji.labeleditor.plugin.renderers;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

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

	protected synchronized void updateLUT(LabelingMapping<L> mapping, LabelEditorTagColors tagColors, LabelEditorTargetComponent targetComponent) {

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

			L label = sortedLabels.get(j);
			Set<Object> labelTags = model.tagging().getTags(label);
			ArrayList<Object> sortedTags = new ArrayList<>(labelTags);
			sortedTags.sort(model.getTagComparator());
			sortedTags.add(LabelEditorTag.DEFAULT);

			labelColors[j] = mixColorsAdditive(label, sortedTags, tagColors, targetComponent, model.tagging());
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
	public synchronized RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<? super IntegerType<?>, ARGBType> converter = (i, o) -> o.set(getLUT()[i.getInteger()]);
		return Converters.convert(model.labeling().getIndexImg(), converter,
				new ARGBType());
	}

	protected synchronized int[] getLUT() {
		return lut;
	}

	public static <L> int mixColorsAdditive(L label, List<Object> tags, LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging) {

		int[] colors = getTagColors(label, tags, tagColors, targetComponent, tagging);
		return ColorMixingUtils.mixColorsAdditive(colors);
	}

	public static <L> int mixColorsOverlay(L label, List<Object> tags, LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging) {

		int[] colors = getTagColors(label, tags, tagColors, targetComponent, tagging);
		return ColorMixingUtils.mixColorsOverlay(colors);
	}

	private static <L> int[] getTagColors(L label, List< Object > tags,
	                                      LabelEditorTagColors tagColors, Object targetComponent, LabelEditorTagging tagging)
	{
		int[] colors = new int[tags.size()];
		for (int i = 0; i < colors.length; i++) {
			Object tag = tags.get(i);
			Object value = tagging.getValue(tag, label);
			int color = tagColors.getColorset(tag).get(targetComponent).get(value);
			colors[i] = color;
		}
		return colors;
	}

	static <L> int[] getTagColors(List< Object > tags, LabelEditorTagColors tagColors, Object targetComponent)
	{
		int[] colors = new int[tags.size()];
		for (int i = 0; i < colors.length; i++) {
			int color = tagColors.getColorset(tags.get(i)).get(targetComponent).get();
			colors[i] = color;
		}
		return colors;
	}

	void printLUT() {
		printLUT(model.labeling().getMapping(), lut);
	}
}
