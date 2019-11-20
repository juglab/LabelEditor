package com.indago.labeleditor.plugin.mode.timeslice;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import com.indago.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorController;

public class TimeSliceLabelEditorBdvPanel<L> extends LabelEditorBdvPanel<L> {

	private TimeSliceLabelEditorController controller = new TimeSliceLabelEditorController();

	@Override
	public TimeSliceLabelEditorController<L> control() {
		return controller;
	}
}
