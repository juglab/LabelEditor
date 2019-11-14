package com.indago.labeleditor.plugin.behaviours;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.roi.labeling.LabelingType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConflictSelectionBehaviours<L> extends SelectionBehaviours<L> {

	@Override
	protected void handleClick(int arg0, int arg1) {
		LabelingType<L> labels = controller.interfaceInstance().getLabelsAtMousePosition(arg0, arg1, model);
		//TODO start collect tagging events, pause listeners
		if (labels != null && labels.size() > 0) {
			selectFirst(labels);
		}
		//TODO resume model listeners and send collected events
	}

	@Override
	protected void handleShiftClick(int arg0, int arg1) {
		handleClick(arg0, arg1);
	}

	@Override
	protected void handleShiftWheelRotation(double direction, boolean isHorizontal, int x, int y) {
		LabelingType<L> labels = controller.interfaceInstance().getLabelsAtMousePosition(x, y, model);
		if(labels.size() == 0) return;
		if(!anySelected(labels)) {
			//TODO start collect tagging events, pause listeners
			selectFirst(labels);
			//TODO resume model listeners and send collected events
			return;
		}
		if ( !isHorizontal ) {
			//TODO start collect tagging events, pause listeners
			if (direction > 0)
				selectNext(labels);
			else
				selectPrevious(labels);
			//TODO resume model listeners and send collected events
		}
	}

	@Override
	protected void selectFirst(LabelingType<L> currentLabels) {
		L label = getFirst(currentLabels);
		if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) {
			deselect(label);
			return;
		}
		Set<L> conflicts = getConflictingLabels(label);
		deselect(conflicts);
		select(label);
	}

	private void deselect(Set<L> labels) {
		labels.forEach(label -> model.tagging().removeTag(LabelEditorTag.SELECTED, label));
	}

	private Set<L> getConflictingLabels(L label) {
		Set<L> res = new HashSet<>();
		model.labels().forEach(labelset -> {
			if(labelset.contains(label)) {
				res.addAll(labelset);
			}
		});
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
					if(model.tagging().getTags(label).contains(LabelEditorTag.SELECTED)) return;
					Set<L> conflicts = getConflictingLabels(label);
					deselect(conflicts);
					select(label);
					return;
				}
			}
		}
	}
}
