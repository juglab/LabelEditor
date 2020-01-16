package sc.fiji.labeleditor.plugin.behaviours.modification;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;

import java.awt.*;

public class LabelingModificationBehaviours<L> extends Behaviours implements LabelEditorBehaviours<L> {

	@Parameter
	private Context context;

	private InteractiveLabeling<L> labeling;

	public LabelingModificationBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-modification");
	}

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {
		behaviours.behaviour((ClickBehaviour) (arg0, arg1) -> getDeleteBehaviour().deleteSelected(),
				"delete selected labels","DELETE" );
	}

	public DeleteLabels getDeleteBehaviour() {
		return new DeleteLabels<>(labeling);
	}

	public SplitLabels getSplitBehaviour() {
		SplitLabels behaviour = new SplitLabels<>(labeling);
		if(context != null) context.inject(behaviour);
		return behaviour;
	}

	public MergeLabels getMergeBehaviour() {
		return new MergeLabels<>(labeling);
	}

}
