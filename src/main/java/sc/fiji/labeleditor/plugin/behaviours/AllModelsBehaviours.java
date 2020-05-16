package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;

import java.util.Set;


public class AllModelsBehaviours {

	@Parameter
	private CommandService commandService;

	private Set<InteractiveLabeling<?>> labelings;
	private LabelEditorInterface labelEditorInterface;

	public void init(Set<InteractiveLabeling<?>> labelings, LabelEditorInterface labelEditorInterface) {
		this.labelings = labelings;
		this.labelEditorInterface = labelEditorInterface;
	}

	public ClickBehaviour getShowOptionsBehaviour() {
		return (arg0, arg1) -> showOptions();
	}

	public void showOptions() {
		commandService.run(ModelOptionsCommand.class, true,
				"labelings", labelings, "labelEditorInterface", labelEditorInterface);
	}
}
