package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.Set;

public class DeleteLabels<L> implements Behaviour {

	private final InteractiveLabeling<L> labeling;

	public DeleteLabels(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void deleteSelected() {
		Set<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		delete(selected, labeling.getLabelingInScope());
		labeling.view().updateOnLabelingChange();
	}

	private static <L> void delete(Set<L> labels, IterableInterval<LabelingType<L>> labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType<L> val = cursor.next();
			val.removeAll(labels);
		}
	}

	static <L> void delete(L label, IterableInterval<LabelingType<L>> labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType<L> val = cursor.next();
			val.remove(label);
		}
	}
}