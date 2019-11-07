package com.indago.labeleditor.action;

import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imglib2.Localizable;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.Collection;

public interface ViewerActionBridge<L> {
	LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model);
	void set3DViewMode(boolean mode3D);
	Localizable getDataPositionAtMouse();
	void update();
	Collection<? extends ActionHandler<L>> getAvailableActions(ActionManager<L> actionManager, LabelEditorModel<L> model, RenderingManager<L> renderer);
}
