package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;

import java.util.ArrayList;
import java.util.List;

public class LabelEditorController<L> {

	private LabelEditorModel<L> model;
	private LabelEditorView<L> view;
	private LabelEditorInterface<L> interfaceInstance;
	private final List<LabelEditorBehaviours> behaviours = new ArrayList<>();

	public void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		this.model = model;
		this.view = view;
		this.interfaceInstance = interfaceInstance;
		view.listeners().add(interfaceInstance::onViewChange);
	}

	public void addDefaultBehaviours() {
		behaviours.addAll(interfaceInstance.getAvailableActions(model, this));
	}

	public void triggerLabelingChange() {
		view.updateOnLabelingChange();
	}

	public LabelEditorInterface<L> interfaceInstance() {
		return interfaceInstance;
	}

	public List<LabelEditorBehaviours> behaviours() {
		return behaviours;
	}
}
