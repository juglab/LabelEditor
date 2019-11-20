package com.indago.labeleditor.plugin.behaviours.modification;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.Behaviour;

import java.util.Set;

public class MergeLabels<L> implements Behaviour {

	private final LabelEditorController<L> controller;
	private final LabelEditorModel<L> model;

	public MergeLabels(LabelEditorModel<L> model, LabelEditorController<L> controller) {
		this.model = model;
		this.controller = controller;
	}

	public void assignSelectedToFirst() {
		Set<L> selected = model.tagging().getLabels(LabelEditorTag.SELECTED);
		assignToFirst(selected, controller.labelingInScope());
		controller.triggerLabelingChange();
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