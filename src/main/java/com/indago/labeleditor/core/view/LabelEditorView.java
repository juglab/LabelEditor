package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.colors.LabelEditorColorset;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
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

	private LabelEditorModel<L> model;
	private final LabelEditorRenderers renderers = new LabelEditorRenderers();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public LabelEditorView() {}

	public LabelEditorView(DefaultLabelEditorModel<L> model) {
		init(model);
	}

	public void init(LabelEditorModel<L> model) {
		this.model = model;
		if(model.labels() == null) return;
		renderers.clear();
		renderers.init(model, this);
		model.tagging().listeners().add(this::onTagChange);
	}

	private void onTagChange(TagChangedEvent tagChangedEvent) {
		updateRenderers();
	}

	void updateRenderers() {
		if(model == null || model.labels() == null) return;
		renderers.forEach(renderer -> renderer.updateOnTagChange(model));
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

	public Listeners< ViewChangeListener > listeners() {
		return listeners;
	}

	public void pauseListeners(){
		listenersPaused = true;
	}
	public void resumeListeners(){
		listenersPaused = false;
	}

	private void notifyListeners() {
		if(listenersPaused) return;
		listeners.list.forEach(listener -> listener.viewChanged(new ViewChangedEvent()));
	}
}
