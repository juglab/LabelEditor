package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorTag;
import com.indago.labeleditor.plugin.renderer.BorderLabelEditorRenderer;
import com.indago.labeleditor.plugin.renderer.DefaultLabelEditorRenderer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO move renderer list into field
//TODO make coloring smarter, e.g. add border color, somehow extendable
public class LabelEditorView<L> extends ArrayList<LabelEditorRenderer<L>> {

	private static int colorMouseOver = ARGBType.rgba(200,200,200,200);
	private static int colorSelected = ARGBType.rgba(0,100,255,200);
	static int colorDefault = ARGBType.rgba(255,255,255,100);
	private final Map<Object, LUTChannel> tagColors = new HashMap<>();
	private LabelEditorModel<L> model;

	public LabelEditorView() {}

	public LabelEditorView(DefaultLabelEditorModel<L> model) {
		init(model);
	}

	public void init(LabelEditorModel<L> model) {
		this.model = model;
		tagColors.clear();
		tagColors.put(LabelEditorTag.NO_TAG, new LUTChannel(colorDefault));
		tagColors.put(LabelEditorTag.SELECTED, new LUTChannel(colorSelected));
		tagColors.put(LabelEditorTag.MOUSE_OVER, new LUTChannel(colorMouseOver));
		clear();
	}


	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	public void setTagColor(Object tag, int color) {
		tagColors.put(tag, new LUTChannel(color));
	}

	public void removeTagColor(Object tag) {
		tagColors.remove(tag);
	}

	public void updateOnTagChange() {
		if(model == null || model.labels() == null) return;
		final LabelingMapping<L> mapping = model.labels().getMapping();
		final Map<L, Set<Object>> tags = model.tagging().get();
		this.forEach(renderer -> renderer.updateOnTagChange(mapping, tags, tagColors));
	}

	public void updateOnLabelingChange() {
		if(model == null || model.labels() == null) return;
		this.forEach(LabelEditorRenderer::updateOnLabelingChange);
	}

	public Map<String, RandomAccessibleInterval> getNamedRenderings() {
		updateOnTagChange();
		Map<String, RandomAccessibleInterval> res = new HashMap<>();
		this.forEach(renderer -> res.put(renderer.getName(), renderer.getOutput()));
		return res;
	}

	public void addDefaultRenderings() {
		//TODO find available renderers by annotation
		add(new DefaultLabelEditorRenderer<>());
		add(new BorderLabelEditorRenderer<>());
	}

	public void initRenderings() {
		forEach(renderer -> renderer.init(model.labels()));
	}
}
