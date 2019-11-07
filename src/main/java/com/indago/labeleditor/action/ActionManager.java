package com.indago.labeleditor.action;

import bdv.util.BdvHandlePanel;
import bvv.util.BvvSource;
import com.indago.labeleditor.action.bdv.BdvActionBridge;
import com.indago.labeleditor.action.bvv.BvvActionBridge;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;

public class ActionManager<L> extends ArrayList<ActionHandler<L>> {

	ViewerActionBridge<L> bridge;
	private RenderingManager<L> renderer;
	private LabelEditorModel<L> model;

	public ActionManager(Object panel, LabelEditorModel<L> model, RenderingManager<L> renderer) {
		if(panel.getClass().isAssignableFrom(BdvHandlePanel.class)) {
			bridge = new BdvActionBridge<L>((BdvHandlePanel) panel);
			init(model, renderer);
			return;
		}
		if(panel.getClass().isAssignableFrom(BvvSource.class)) {
			bridge = new BvvActionBridge<L>((BvvSource) panel);
			init(model, renderer);
			set3DViewMode(true);
			return;
		}
		throw new NotImplementedException();
	}

	private void init(LabelEditorModel<L> model, RenderingManager<L> renderer) {
		this.renderer = renderer;
		this.model = model;
	}

	public void addDefaultActionHandlers() {
		addAll(bridge.getAvailableActions(this, model, renderer));
	}

	protected void updateLabelRendering() {
		renderer.update();
		bridge.update();
	}

	public ViewerActionBridge<L> getBridge() {
		return bridge;
	}

	public void set3DViewMode(boolean mode3D) {
		bridge.set3DViewMode(mode3D);
	}
}
