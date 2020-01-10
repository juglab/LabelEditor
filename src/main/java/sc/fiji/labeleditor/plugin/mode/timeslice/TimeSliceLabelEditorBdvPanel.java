package sc.fiji.labeleditor.plugin.mode.timeslice;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

public class TimeSliceLabelEditorBdvPanel extends LabelEditorBdvPanel {

	@Override
	public <L> LabelEditorController<L> createController() {
		return new TimeSliceLabelEditorController<>();
	}
}
