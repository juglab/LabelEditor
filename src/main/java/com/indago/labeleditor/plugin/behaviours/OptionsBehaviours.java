package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public class OptionsBehaviours extends Behaviours implements LabelEditorBehaviours {

	@Parameter
	CommandService commandService;

	protected LabelEditorView view;

	public OptionsBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-options");
	}

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller, LabelEditorView view) {
		this.view = view;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ClickBehaviour getShowOptionsBehaviour() {
		return (arg0, arg1) -> showOptions();
	}

	public void showOptions() {
		commandService.run(LabelEditorOptionsCommand.class, true, "view", view);
	}

}
