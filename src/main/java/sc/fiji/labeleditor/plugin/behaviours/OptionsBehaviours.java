package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.awt.*;

public class OptionsBehaviours<L> implements LabelEditorBehaviours<L> {

	@Parameter
	private CommandService commandService;

	private LabelEditorView<L> view;
	private LabelEditorInterface labelEditorInterface;

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.view = labeling.view();
		this.labelEditorInterface = labeling.interfaceInstance();
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ClickBehaviour getShowOptionsBehaviour() {
		return (arg0, arg1) -> showOptions();
	}

	public void showOptions() {
		commandService.run(LabelEditorOptionsCommand.class, true,
				"view", view,
				"labelEditorInterface", labelEditorInterface);
	}

}
