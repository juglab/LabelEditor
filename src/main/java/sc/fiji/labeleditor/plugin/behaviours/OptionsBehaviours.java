package sc.fiji.labeleditor.plugin.behaviours;

import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
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
