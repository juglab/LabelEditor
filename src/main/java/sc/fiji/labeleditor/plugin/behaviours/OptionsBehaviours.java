package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.awt.*;

public class OptionsBehaviours<L> extends Behaviours implements LabelEditorBehaviours<L> {

	@Parameter
	private CommandService commandService;

	protected LabelEditorView<L> view;

	public OptionsBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-options");
	}

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.view = labeling.view();
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
