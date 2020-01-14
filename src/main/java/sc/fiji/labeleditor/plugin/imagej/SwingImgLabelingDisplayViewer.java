package sc.fiji.labeleditor.plugin.imagej;

import net.imglib2.roi.labeling.ImgLabeling;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;

/**
 * This class creates a {@link LabelEditorBdvPanel} for a {@link LabelEditorModel}.
 */
@Plugin(type = DisplayViewer.class, priority = 1.0)
public class SwingImgLabelingDisplayViewer extends EasySwingDisplayViewer<ImgLabeling> implements Disposable {

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
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel();
		context.inject(panel);
		panel.add(new DefaultLabelEditorModel<>(labeling));
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