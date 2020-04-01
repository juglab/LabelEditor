package sc.fiji.labeleditor.plugin.mode.timeslice;

import bdv.util.BdvOptions;
import org.scijava.Context;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
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
	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface interfaceInstance) {
		TimeSliceInteractiveLabeling<L> interactiveLabeling = new TimeSliceInteractiveLabeling<>(model, view, interfaceInstance);
		if(context() != null) context().inject(interactiveLabeling);
		interactiveLabeling.initialize();
		return interactiveLabeling;
	}
}
