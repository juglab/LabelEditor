package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.Collection;

public interface LabelEditorInterface<L> {
	LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model);
	void set3DViewMode(boolean mode3D);
	Collection<? extends LabelEditorBehaviours> getAvailableActions(LabelEditorModel<L> model, LabelEditorController<L> controller);
	void onViewChange(ViewChangedEvent viewChangedEvent);
}
