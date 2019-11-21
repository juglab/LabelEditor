package com.indago.labeleditor.application.display;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imglib2.roi.labeling.ImgLabeling;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;

import javax.swing.*;

/**
 * This class creates a {@LabelEditorBdvPanel} for a {@link LabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class)
public class SwingImgLabelingDisplayViewer extends EasySwingDisplayViewer<ImgLabeling> {

	@Parameter
	Context context;

	public SwingImgLabelingDisplayViewer() {
		super(ImgLabeling.class);
	}

	@Override
	protected boolean canView(ImgLabeling labeling) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(ImgLabeling labeling) {
		System.out.println("context? " + context);
		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
		context.inject(panel);
		panel.init(labeling);
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