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

public class DeleteLabels<L> implements Behaviour {

	private final LabelEditorController controller;
	private final LabelEditorModel<L> model;

	public DeleteLabels(LabelEditorModel<L> model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void deleteSelected() {
		Set selected = model.tagging().getLabels(LabelEditorTag.SELECTED);
		delete(selected, model.labels());
		controller.triggerLabelingChange();
	}

	static <L> void delete(Set<L> labels, ImgLabeling<L, IntType> labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType val = cursor.next();
			val.removeAll(labels);
		}
	}

	static <L> void delete(L label, ImgLabeling<L, IntType> labeling) {
		Cursor<LabelingType<L>> cursor = labeling.cursor();
		while (cursor.hasNext()) {
			LabelingType val = cursor.next();
			val.remove(label);
		}
	}
}