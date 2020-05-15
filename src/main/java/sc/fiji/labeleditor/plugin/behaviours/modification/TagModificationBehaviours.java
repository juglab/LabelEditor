package sc.fiji.labeleditor.plugin.behaviours.modification;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;

import java.awt.*;

public class TagModificationBehaviours<L> implements LabelEditorBehaviours<L> {

	protected InteractiveLabeling<L> labeling;

	@Parameter
	Context context;

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public TagByProperty getTagByPropertyBehaviour() {
		return new TagByProperty<>(labeling);
	}

}
