package com.indago.labeleditor.display;

import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultLabelEditorRenderer<L> implements LabelEditorRenderer<L> {

	private final LabelEditorModel<L> model;
	protected int[] lut;

	private static int colorMouseOver = ARGBType.rgba(50,50,50,100);
	private static int colorSelected = ARGBType.rgba(255,50,50,100);
	private final Map<Object, LUTChannel> tagColors;

	public DefaultLabelEditorRenderer(ImgLabeling<L, IntType> labeling) {
		this(new DefaultLabelEditorModel<>(labeling));
	}

	public DefaultLabelEditorRenderer(LabelEditorModel<L> model) {
		this.model = model;
		tagColors = new HashMap<>();
		tagColors.put(LabelEditorTag.SELECTED, new LUTChannel(colorSelected));
		tagColors.put(LabelEditorTag.MOUSE_OVER, new LUTChannel(colorMouseOver));
	}

	@Override
	public synchronized void update() {

		if(model.getLabels() == null) return;

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[model.getLabels().getMapping().numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<L> labels = model.getLabels().getMapping().labelsAtIndex(i);

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<Object> mytags = filterTagsByLabels(model.getTags(), labels);

			lut[i] = mixColors(mytags, tagColors);

		}

		System.out.println(Arrays.toString(lut));

		this.lut = lut;
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getRenderedLabels() {
		update();
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(model.getLabels().getIndexImg(), converter, new ARGBType() );
	}

	public int[] getLUT() {
		return lut;
	}

	public LabelEditorModel<L> getModel() {
		return model;
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
			red = (red*alpha+newred*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			green = (green*alpha+newgreen*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			blue = (blue*alpha+newblue*newalpha*(1-alpha))/(alpha + newalpha*(1-alpha));
			alpha = alpha + newalpha*(1-alpha);
		}
		return ARGBType.rgba((int)red, (int)green, (int)blue, (int)(alpha*255));
	}

	@Override
	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	@Override
	public void setTagColor(Object tag, int color) {
		tagColors.put(tag, new LUTChannel(color));
		//TODO propagateEvent...
	}

	@Override
	public void removeTagColor(Object tag) {
		tagColors.remove(tag);
		//TODO propagateEvent...
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
