package com.indago.labeleditor.plugin.behaviours.modification;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public class TagModificationBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;
	protected LabelEditorController controller;

	@Parameter
	Context context;

	public TagModificationBehaviours() {
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

	public TagByProperty getTagByPropertyBehaviour() {
		return new TagByProperty(model, controller);
	}

}
