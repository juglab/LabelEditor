package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;
import java.util.List;

public class LabelEditorController<L> {

	private LabelEditorInterface<L> interfaceInstance;
	private LabelEditorView<L> renderer;
	private LabelEditorModel<L> model;
	private final List<LabelEditorActions> actions = new ArrayList<>();

	public void init(LabelEditorInterface<L> interfaceInstance, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		this.interfaceInstance = interfaceInstance;
		this.renderer = renderer;
		this.model = model;
		renderer.listeners().add(interfaceInstance::onViewChange);
	}

	public void addDefaultActionHandlers() {
		actions.addAll(interfaceInstance.getAvailableActions(this, model, renderer));
	}

	public void triggerLabelingChange() {
		renderer.updateOnLabelingChange();
	}

	public LabelEditorInterface<L> viewer() {
		return interfaceInstance;
	}

	public void set3DViewMode(boolean mode3D) {
		interfaceInstance.set3DViewMode(mode3D);
	}

	public List<LabelEditorActions> actions() {
		return actions;
	}
}
