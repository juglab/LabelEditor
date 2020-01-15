package sc.fiji.labeleditor.plugin.imagej;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;

/**
 * This class creates a {@link LabelEditorBdvPanel} for a {@link LabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class)
public class SwingLabelEditorModelDisplayViewer extends EasySwingDisplayViewer< LabelEditorModel > {

	@Parameter
	Context context;

	public SwingLabelEditorModelDisplayViewer() {
		super(LabelEditorModel.class);
	}

	@Override
	protected boolean canView(LabelEditorModel model) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(LabelEditorModel model) {
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(context);
		panel.add(model);
		return panel;
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