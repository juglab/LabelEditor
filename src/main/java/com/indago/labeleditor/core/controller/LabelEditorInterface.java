package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.Collection;

public interface LabelEditorInterface<L> {
	LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model);
	void set3DViewMode(boolean mode3D);
//	Localizable getDataPositionAtMouse();
	void update();
	Collection<? extends LabelEditorActions> getAvailableActions(LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer);
}
