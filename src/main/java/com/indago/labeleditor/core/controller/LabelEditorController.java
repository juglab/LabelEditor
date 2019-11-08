package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;

public class LabelEditorController<L> extends ArrayList<LabelEditorActions> {

	LabelEditorInterface<L> interfaceInstance;
	private LabelEditorView<L> renderer;
	private LabelEditorModel<L> model;

	public void init(LabelEditorInterface interfaceInstance, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		this.interfaceInstance = interfaceInstance;
		this.renderer = renderer;
		this.model = model;
	}

	public void addDefaultActionHandlers() {
		addAll(interfaceInstance.getAvailableActions(this, model, renderer));
	}

	public void triggerTagChange() {
		renderer.updateOnTagChange();
		interfaceInstance.update();
	}

	public void triggerLabelingChange() {
		renderer.updateOnLabelingChange();
		interfaceInstance.update();
	}

	public LabelEditorInterface<L> viewer() {
		return interfaceInstance;
	}

	public void set3DViewMode(boolean mode3D) {
		interfaceInstance.set3DViewMode(mode3D);
	}
}
