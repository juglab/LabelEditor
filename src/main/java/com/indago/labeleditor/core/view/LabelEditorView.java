package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorTag;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//TODO make coloring smarter, e.g. add border color, somehow extendable
public class LabelEditorView<L> {

	private static int colorMouseOver = ARGBType.rgba(200,200,200,200);
	private static int colorSelected = ARGBType.rgba(0,100,255,200);
	static int colorDefault = ARGBType.rgba(255,255,255,100);
	private final LabelEditorColors tagColors = new LabelEditorColors();
	private LabelEditorModel<L> model;

	private final LabelEditorRenderers renderers = new LabelEditorRenderers();

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
		renderers.clear();
		renderers.init(this, model);
	}


	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	public void update() {
		updateOnTagChange();
	}

	public void updateOnTagChange() {
		if(model == null || model.labels() == null) return;
		final LabelingMapping<L> mapping = model.labels().getMapping();
		final Map<L, Set<Object>> tags = model.tagging().get();
		renderers.forEach(renderer -> renderer.updateOnTagChange(mapping, tags, tagColors));
	}

	public void updateOnLabelingChange() {
		if(model == null || model.labels() == null) return;
		renderers.forEach(LabelEditorRenderer::updateOnLabelingChange);
	}

	public LabelEditorRenderers renderers() {
		return renderers;
	}

	public LabelEditorColors colors() {
		return tagColors;
	}
}
