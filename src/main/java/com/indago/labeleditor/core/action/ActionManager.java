package com.indago.labeleditor.core.action;

import com.indago.labeleditor.core.display.RenderingManager;
import com.indago.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;

public class ActionManager<L> extends ArrayList<ActionHandler<L>> {

	ViewerInstance<L> viewerInstance;
	private RenderingManager<L> renderer;
	private LabelEditorModel<L> model;

	public void init(ViewerInstance viewer, LabelEditorModel<L> model, RenderingManager<L> renderer) {
		this.viewerInstance = viewer;
		this.renderer = renderer;
		this.model = model;
	}

	public void addDefaultActionHandlers() {
		addAll(viewerInstance.getAvailableActions(this, model, renderer));
	}

	public void triggerChange() {
		renderer.update();
		viewerInstance.update();
	}

	public ViewerInstance<L> viewer() {
		return viewerInstance;
	}

	public void set3DViewMode(boolean mode3D) {
		viewerInstance.set3DViewMode(mode3D);
	}
}
