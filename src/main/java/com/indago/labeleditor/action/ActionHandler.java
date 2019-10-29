package com.indago.labeleditor.action;

import net.imglib2.Localizable;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;

public interface ActionHandler<L> {
	void init();

	LabelingType<L> getLabelsAtMousePosition(MouseEvent e);

	void set3DViewMode(boolean mode3D);

	Localizable getDataPositionAtMouse();
}
