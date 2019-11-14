package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.colors.ColorChangedEvent;
import com.indago.labeleditor.core.model.tagging.TagChangedEvent;
import org.scijava.listeners.Listeners;

public class LabelEditorView<L> {

	private LabelEditorModel<L> model;
	private final LabelEditorRenderers renderers = new LabelEditorRenderers();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public LabelEditorView() {}

	public LabelEditorView(LabelEditorModel<L> model) {
		init(model);
	}

	public void init(LabelEditorModel<L> model) {
		this.model = model;
		if(model.labels() == null) return;
		renderers.clear();
		renderers.init(model, this);
		model.tagging().listeners().add(this::onTagChange);
		model.colors().listeners().add(this::onColorChange);
		notifyListeners();
	}

	private void onColorChange(ColorChangedEvent colorChangedEvent) {
		updateRenderers();
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
		renderers.forEach(renderer -> renderer.updateOnTagChange(model));
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
