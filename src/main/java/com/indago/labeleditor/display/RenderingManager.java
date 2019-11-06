package com.indago.labeleditor.display;

import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderingManager<L> extends ArrayList<LabelEditorRenderer<L>> {

	private static int colorMouseOver = ARGBType.rgba(50,50,50,100);
	private static int colorSelected = ARGBType.rgba(255,50,50,100);
	private final Map<Object, LUTChannel> tagColors = new HashMap<>();
	private LabelEditorModel<L> model;

	public RenderingManager(DefaultLabelEditorModel<L> model) {
		init(model);
	}

	public RenderingManager() {}

	public void init(ImgLabeling<L, IntType> labeling) {
		init(new DefaultLabelEditorModel<>(labeling));
	}

	public void init(LabelEditorModel<L> model) {
		this.model = model;
		tagColors.clear();
		tagColors.put(LabelEditorTag.SELECTED, new LUTChannel(colorSelected));
		tagColors.put(LabelEditorTag.MOUSE_OVER, new LUTChannel(colorMouseOver));
		clear();
		add(new DefaultLabelEditorRenderer<>());
	}


	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	public void setTagColor(Object tag, int color) {
		tagColors.put(tag, new LUTChannel(color));
		//TODO propagateEvent...
	}

	public void removeTagColor(Object tag) {
		tagColors.remove(tag);
		//TODO propagateEvent...
	}

	public void update() {
		if(model == null || model.labels() == null) return;
		LabelingMapping<L> mapping = model.labels().getMapping();
		this.forEach(renderer -> {
			renderer.update(mapping, model.tagging().get(), tagColors);
		});
	}

	public List<RandomAccessibleInterval> getRenderings() {
		update();
		List<RandomAccessibleInterval> res = new ArrayList<>();
		this.forEach(renderer -> res.add(renderer.getRenderedLabels(model.labels())));
		return res;
	}
}
