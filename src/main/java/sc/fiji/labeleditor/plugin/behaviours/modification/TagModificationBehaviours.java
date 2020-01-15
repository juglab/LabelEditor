package sc.fiji.labeleditor.plugin.behaviours.modification;

import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
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
	public void init(InteractiveLabeling labeling) {
		this.model = labeling.model();
		this.controller = labeling.control();
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public TagByProperty getTagByPropertyBehaviour() {
		return new TagByProperty(model, controller);
	}

}
