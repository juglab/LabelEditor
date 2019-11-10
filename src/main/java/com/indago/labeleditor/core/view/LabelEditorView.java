package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.model.tagging.TagChangedEvent;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import org.scijava.listeners.Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LabelEditorView<L> {

	private static int colorMouseOver = ARGBType.rgba(200,200,200,200);
	private static int colorSelected = ARGBType.rgba(0,100,255,200);
	static int colorDefault = ARGBType.rgba(255,255,255,100);
	private final LabelEditorColors tagColors = new LabelEditorColors(this);
	private LabelEditorModel<L> model;

	private final LabelEditorRenderers renderers = new LabelEditorRenderers();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();

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
		renderers.init(model, this);
		model.tagging().listeners().add(this::onTagChange);
	}

	private void onTagChange(TagChangedEvent tagChangedEvent) {
		updateRenderers();
	}

	public List<LUTChannel> getVirtualChannels() {
		return new ArrayList<>(tagColors.values());
	}

	void updateRenderers() {
		if(model == null || model.labels() == null) return;
		final LabelingMapping<L> mapping = model.labels().getMapping();
		final Map<L, Set<Object>> tags = model.tagging().get();
		renderers.forEach(renderer -> renderer.updateOnTagChange(mapping, tags, tagColors));
		notifyListeners();
	}

	public void updateOnLabelingChange() {
		if(model == null || model.labels() == null) return;
		renderers.forEach(LabelEditorRenderer::updateOnLabelingChange);
		notifyListeners();
	}

	public LabelEditorRenderers renderers() {
		return renderers;
	}

	public LabelEditorColors colors() {
		return tagColors;
	}

	public Listeners< ViewChangeListener > listeners() {
		return listeners;
	}

	private void notifyListeners() {
		listeners.list.forEach(listener -> listener.viewChanged(new ViewChangedEvent()));
	}
}
