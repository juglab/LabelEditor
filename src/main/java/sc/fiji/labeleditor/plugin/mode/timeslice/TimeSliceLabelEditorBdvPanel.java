package sc.fiji.labeleditor.plugin.mode.timeslice;

import org.scijava.Context;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

public class TimeSliceLabelEditorBdvPanel extends LabelEditorBdvPanel {

	public TimeSliceLabelEditorBdvPanel() {
		super();
	}

	public TimeSliceLabelEditorBdvPanel(Context context) {
		super(context);
	}

	@Override
	public <L> LabelEditorController<L> createController() {
		return new TimeSliceLabelEditorController<>();
	}
}
