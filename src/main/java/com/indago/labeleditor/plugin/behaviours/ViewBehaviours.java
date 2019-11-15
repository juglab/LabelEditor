package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.modification.DeleteLabels;
import com.indago.labeleditor.plugin.behaviours.modification.MergeLabels;
import com.indago.labeleditor.plugin.behaviours.modification.SplitLabels;
import com.indago.labeleditor.plugin.behaviours.view.ViewLabels;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public class ViewBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;
	protected LabelEditorController controller;

	@Parameter
	Context context;

	public ViewBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-view");
	}

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ViewLabels getViewBehaviour() {
		return new ViewLabels(model, controller);
	}

}
