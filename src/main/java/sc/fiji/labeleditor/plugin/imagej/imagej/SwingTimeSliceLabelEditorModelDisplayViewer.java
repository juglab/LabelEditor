package sc.fiji.labeleditor.plugin.imagej.imagej;

import sc.fiji.labeleditor.core.LabelEditorPanel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorBdvPanel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

import javax.swing.*;

/**
 * This class creates a {@link TimeSliceLabelEditorBdvPanel} for a {@link TimeSliceLabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class, priority = 1.0)
public class SwingTimeSliceLabelEditorModelDisplayViewer extends EasySwingDisplayViewer<TimeSliceLabelEditorModel> {

	@Parameter
	Context context;

	public SwingTimeSliceLabelEditorModelDisplayViewer() {
		super(TimeSliceLabelEditorModel.class);
	}

	@Override
	protected boolean canView(TimeSliceLabelEditorModel model) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(TimeSliceLabelEditorModel model) {
		LabelEditorPanel panel = new TimeSliceLabelEditorBdvPanel<>();
		context.inject(panel);
		panel.init(model);
		return (JPanel) panel.get();
	}

	@Override
	public void redraw()
	{
		//TODO do I need to update the panel / create a new panel?
		getWindow().pack();
	}

	@Override
	public void redoLayout()
	{
		// ignored
	}

	@Override
	public void setLabel(final String s)
	{
		// ignored
	}
}