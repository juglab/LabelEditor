package com.indago.labeleditor.application.display;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

import javax.swing.*;

/**
 * This class creates a {@LabelEditorBdvPanel} for a {@link LabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class)
public class SwingLabelEditorModelDisplayViewer extends EasySwingDisplayViewer< LabelEditorModel > {

	@Parameter
	Context context;

	private LabelEditorModel model = null;

	public SwingLabelEditorModelDisplayViewer() {
		super(LabelEditorModel.class);
	}

	@Override
	protected boolean canView(LabelEditorModel model) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(LabelEditorModel model) {
		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
		context.inject(panel);
		panel.init(model);
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