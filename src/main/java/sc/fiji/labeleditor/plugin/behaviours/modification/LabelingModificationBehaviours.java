package sc.fiji.labeleditor.plugin.behaviours.modification;

import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public class LabelingModificationBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;
	protected LabelEditorController controller;

	@Parameter
	Context context;

	public LabelingModificationBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-modification");
	}

	@Override
	public void init(LabelEditorModel model, LabelEditorController controller, LabelEditorView view) {
		this.model = model;
		this.controller = controller;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {
		behaviours.behaviour((ClickBehaviour) (arg0, arg1) -> getDeleteBehaviour().deleteSelected(),
				"delete selected labels","DELETE" );
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

}