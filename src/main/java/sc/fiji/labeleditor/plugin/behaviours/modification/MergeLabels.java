package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.Set;

public class MergeLabels<L> implements Behaviour {

	private final InteractiveLabeling<L> labeling;

	public MergeLabels(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void assignSelectedToFirst() {
		Set<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		assignToFirst(selected, labeling.control().getLabelingInScope());
		labeling.view().updateOnLabelingChange();
	}

	private static <L> void assignToFirst(Set<L> labels, IterableInterval<LabelingType<L>> labeling) {
		L first = labels.iterator().next();
		labels.remove(first);
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType<L> val = cursor.next();
			if(val.removeAll(labels)) {
				val.add(first);
			}

		}
	}

}