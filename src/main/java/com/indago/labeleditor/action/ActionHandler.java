package com.indago.labeleditor.action;

import net.imglib2.roi.labeling.LabelingType;

public interface ActionHandler<L> {
	void init();

	LabelingType<L> getLabelsAtMousePosition();
}
