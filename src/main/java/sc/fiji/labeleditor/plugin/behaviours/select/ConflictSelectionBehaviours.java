package sc.fiji.labeleditor.plugin.behaviours.select;

import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConflictSelectionBehaviours<L> extends SelectionBehaviours<L> {

	@Override
	protected void selectFirstLabel(int x, int y) {
		LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling.model());
		if (labels != null && labels.size() > 0) {
			selectFirst(labels);
		}
	}

	@Override
	protected void addFirstLabelToSelection(int x, int y) {
		selectFirstLabel(x, y);
	}

	@Override
	protected void toggleLabelSelection(boolean forwardDirection, int x, int y) {
		LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling.model());
		if(labels.size() == 0) return;
		if(!anySelected(labels)) {
			selectFirst(labels);
			return;
		}
		if (forwardDirection) {
			selectNext(labels);
		}
		else {
			selectPrevious(labels);
		}
	}

	@Override
	protected void selectFirst(LabelingType<L> labels) {
		L label = getFirst(labels);
		if(labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED)) {
			deselect(label);
			return;
		}
		Set<L> conflicts = getConflictingLabels(label);
		deselect(conflicts);
		select(label);
	}

	private void deselect(Set<L> labels) {
		labels.forEach(label -> labeling.model().tagging().removeTagFromLabel(LabelEditorTag.SELECTED, label));
	}

	private Set<L> getConflictingLabels(L label) {
		Set<L> res = new HashSet<>();
		for (int i = 0; i < labeling.model().labeling().getMapping().numSets(); i++) {
			Set<L> labelset = labeling.model().labeling().getMapping().labelsAtIndex(i);
			if(labelset.contains(label)) {
				res.addAll(labelset);
			}
		}
		return res;
	}

	@Override
	protected void selectNext(Collection<L> labels) {

		boolean foundSelected = false;
		for (Iterator<L> iterator = labels.iterator(); iterator.hasNext(); ) {
			L label = iterator.next();
			if (isSelected(label)) {
				foundSelected = true;
			} else {
				if (foundSelected) {
					if(labeling.model().tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
					Set<L> conflicts = getConflictingLabels(label);
					labeling.model().tagging().pauseListeners();
					deselect(conflicts);
					select(label);
					labeling.model().tagging().resumeListeners();
					return;
				}
			}
		}
	}
}
