package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.LabelEditorOptions;
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

public class ModificationBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;
	protected LabelEditorController controller;

	@Parameter
	Context context;

	public ModificationBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-modification");
	}

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public DeleteLabels getDeleteBehaviour() {
		return new DeleteLabels(model, controller);
	}

	public SplitLabels getSplitBehaviour() {
		SplitLabels behaviour = new SplitLabels(model, controller);
		if(context != null) context.inject(behaviour);
		return behaviour;
	}

	public MergeLabels getMergeBehaviour() {
		return new MergeLabels(model, controller);
	}

	public ViewLabels getViewBehaviour() {
		return new ViewLabels(model, controller);
	}

}
