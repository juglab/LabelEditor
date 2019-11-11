package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imglib2.Cursor;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.util.Set;

public class ModificationBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected final LabelEditorModel model;
	protected final LabelEditorController controller;

	public ModificationBehaviours(LabelEditorModel model, LabelEditorController controller) {
		super(new InputTriggerConfig(), "labeleditor-modification");
		this.model = model;
		this.controller = controller;
	}

	public DeleteLabelsBehaviour getDeleteBehaviour() {
		return new DeleteLabelsBehaviour();
	}

	public class DeleteLabelsBehaviour implements Behaviour {
		public void delete(Set labels) {
			Cursor<LabelingType> cursor = model.labels().cursor();
			while(cursor.hasNext()) {
				LabelingType val = cursor.next();
				val.removeAll(labels);
			}
			controller.triggerLabelingChange();
		}

	}
}
