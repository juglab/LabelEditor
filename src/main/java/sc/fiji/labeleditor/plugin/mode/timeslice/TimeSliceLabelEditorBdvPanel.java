package sc.fiji.labeleditor.plugin.mode.timeslice;

import bdv.util.BdvOptions;
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

	public TimeSliceLabelEditorBdvPanel(BdvOptions options) {
		super(options);
	}

	public TimeSliceLabelEditorBdvPanel(Context context, BdvOptions options) {
		super(context, options);
	}

	@Override
	public <L> LabelEditorController<L> createController() {
		return new TimeSliceLabelEditorController<>();
	}
}
