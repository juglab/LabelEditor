package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface LabelEditorInterface<L> {
	LabelingType<L> getLabelsAtMousePosition();
	LabelingType<L> findLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model);
	void set3DViewMode(boolean mode3D);
	void installBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller);
	void onViewChange(ViewChangedEvent viewChangedEvent);

	Behaviours behaviours();

	Component getComponent();
}
