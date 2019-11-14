package com.indago.labeleditor.plugin.behaviours.modification;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imglib2.Cursor;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.Behaviour;

import java.util.Set;

public class MergeLabels<L> implements Behaviour {

	private final LabelEditorController controller;
	private final LabelEditorModel<L> model;

	public MergeLabels(LabelEditorModel<L> model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void assignSelectedToFirst() {
		Set selected = model.tagging().getLabels(LabelEditorTag.SELECTED);
		assignToFirst(selected, model.labels());
		controller.triggerLabelingChange();
	}

	static <L> void assignToFirst(Set<L> labels, ImgLabeling<L, IntType> labeling) {
		L first = labels.iterator().next();
		labels.remove(first);
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType val = cursor.next();
			if(val.removeAll(labels)) {
				val.add(first);
			}

		}
	}

}