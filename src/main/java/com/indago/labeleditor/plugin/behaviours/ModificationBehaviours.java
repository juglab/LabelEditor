package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.modification.DeleteLabels;
import com.indago.labeleditor.plugin.behaviours.modification.SplitSelectedLabels;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

public class ModificationBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected final LabelEditorModel model;
	protected final LabelEditorController controller;

	@Parameter
	Context context;

	public ModificationBehaviours(LabelEditorModel model, LabelEditorController controller) {
		super(new InputTriggerConfig(), "labeleditor-modification");
		this.model = model;
		this.controller = controller;
	}

	public DeleteLabels getDeleteBehaviour() {
		return new DeleteLabels(model, controller);
	}

	public SplitSelectedLabels getSplitBehaviour() {
		SplitSelectedLabels behaviour = new SplitSelectedLabels(model, controller);
		if(context != null) context.inject(behaviour);
		return behaviour;
	}

}
