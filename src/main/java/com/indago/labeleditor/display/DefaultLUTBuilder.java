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

	private static int colorBG = ARGBType.rgba(0,0,255,150);
	private static int colorLabeled = ARGBType.rgba(255,0,0,150);
	private static int colorSelected = ARGBType.rgba(0,255,0,150);
	private static int colorLeadSelected = ARGBType.rgba(255,255,0,150);
	private final Map<Object, LUTChannel> tagColors;

	public DefaultLUTBuilder() {
		tagColors = new HashMap<>();
		tagColors.put(LabelEditorTag.SELECTED, new LUTChannel(colorSelected));
		tagColors.put(LabelEditorTag.MOUSE_OVER, new LUTChannel(colorLabeled));
		tagColors.put(LabelEditorTag.LEAD_SELECTED, new LUTChannel(colorLeadSelected));
	}

	@Override
	public int[] build(LabelEditorModel model) {

		if(model.getLabels() == null) return new int[0];

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[model.getLabels().getMapping().numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<U> labels = model.getLabels().getMapping().labelsAtIndex(i);

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<Object> mytags = filterTagsByLabels(model.getTags(), labels);

			lut[i] = mixColors(mytags);

		}

		return lut;
	}

	private int mixColors(Set<Object> mytags) {
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
		return ARGBType.rgba(red, green, blue, alpha);
	}

	@Override
	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	@Override
	public void setColor(Object tag, int color) {
		tagColors.put(tag, new LUTChannel(color));
	}

	@Override
	public void removeColor(Object tag) {
		tagColors.remove(tag);
	}

	private Set<Object> filterTagsByLabels(Map<U, Set<Object>> tags, Set<U> labels) {
		return tags.entrySet().stream().filter(entry -> labels.contains(entry.getKey())).map(Map.Entry::getValue).flatMap(Set::stream).collect(Collectors.toSet());
	}

}
