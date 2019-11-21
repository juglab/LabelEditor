package sc.fiji.labeleditor.plugin.mode.timeslice;

import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

public class TimeSliceLabelEditorBdvPanel<L> extends LabelEditorBdvPanel<L> {

	private TimeSliceLabelEditorController controller = new TimeSliceLabelEditorController();

	@Override
	public TimeSliceLabelEditorController<L> control() {
		return controller;
	}
}
