package com.indago.labeleditor.action;

import net.imglib2.Localizable;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class ActionManager<L> extends ArrayList<ActionHandler<L>> {
	public Localizable getDataPositionAtMouse() {
		return get(0).getDataPositionAtMouse();
	}

	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e) {
		return get(0).getLabelsAtMousePosition(e);
	}
}
