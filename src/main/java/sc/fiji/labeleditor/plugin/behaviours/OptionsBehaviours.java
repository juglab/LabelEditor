package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;

import java.awt.*;
import java.util.Collections;

public class OptionsBehaviours<L> implements LabelEditorBehaviours<L> {

	@Parameter
	private CommandService commandService;

	private LabelEditorInterface labelEditorInterface;
	private InteractiveLabeling<L> labeling;

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
		this.labelEditorInterface = labeling.interfaceInstance();
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ClickBehaviour getShowOptionsBehaviour() {
		return (arg0, arg1) -> showOptions();
	}

	public void showOptions() {
		commandService.run(ModelOptionsCommand.class, true,
				"labelings", Collections.singleton(labeling), "labelEditorInterface", labelEditorInterface);
	}
}
