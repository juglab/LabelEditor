package sc.fiji.labeleditor.plugin.behaviours.modification;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;

import java.util.Set;

public class DeleteLabels<L> implements Behaviour {

	private final LabelEditorController<L> controller;
	private final LabelEditorModel<L> model;

	public DeleteLabels(LabelEditorModel<L> model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void deleteSelected() {
		Set selected = model.tagging().getLabels(LabelEditorTag.SELECTED);
		delete(selected, controller.labelingInScope());
		controller.triggerLabelingChange();
	}

	static <L> void delete(Set<L> labels, IterableInterval labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType val = cursor.next();
			val.removeAll(labels);
		}
	}

	static <L> void delete(L label, IterableInterval<LabelingType<L>> labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType val = cursor.next();
			val.remove(label);
		}
	}
}