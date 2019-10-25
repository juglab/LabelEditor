package com.indago.labeleditor.display;

import com.indago.labeleditor.LUTChannel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DefaultLUTBuilder<U> implements LUTBuilder<U> {

	private static ARGBType colorBG = new ARGBType(ARGBType.rgba(0,0,255,255));
	private static ARGBType colorLabeled = new ARGBType(ARGBType.rgba(255,0,0,255));
	private static ARGBType colorSelected = new ARGBType(ARGBType.rgba(0,255,0,255));
	private static ARGBType colorLeadSelected = new ARGBType(ARGBType.rgba(255,255,0,255));
	private final Map<Object, LUTChannel> tagColors;

	public DefaultLUTBuilder() {
		tagColors = new HashMap<>();
		tagColors.put(LabelEditorTag.SELECTED, new LUTChannel(colorSelected));
		tagColors.put(LabelEditorTag.VISIBLE, new LUTChannel(colorLabeled));
		tagColors.put(LabelEditorTag.LEAD_SELECTED, new LUTChannel(colorLeadSelected));
	}

	@Override
	public int[] build(LabelEditorModel model) {

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[model.getLabels().getMapping().numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<U> labels = model.getLabels().getMapping().labelsAtIndex(i);

			// distinguish between background index and labeled indices
			lut[i] = labels.size() > 0 ? colorLabeled.get() : colorBG.get();

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<LabelEditorTag> mytags = filterTagsByLabels(model.getTags(), labels);

			int red = 0;
			int green = 0;
			int blue = 0;
			int alpha = 0;
			boolean first = true;
			for (LabelEditorTag tag : mytags) {
				if (first && tagColors.get(tag) != null) {
					ARGBType color = tagColors.get(tag).getColor();
					first = false;
				}
			}
			// set the color depending on the existence of specific tags
			if(mytags.contains(LabelEditorTag.SELECTED)) {
				lut[i] = colorSelected.get();
			}
		}

		return lut;
	}

	@Override
	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	private Set<LabelEditorTag> filterTagsByLabels(Map<U, Set<LabelEditorTag>> tags, Set<U> labels) {
		return tags.entrySet().stream().filter(entry -> labels.contains(entry.getKey())).map(Map.Entry::getValue).flatMap(Set::stream).collect(Collectors.toSet());
	}

}
