/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.imagej;

import bdv.util.AxisOrder;
import bdv.util.BdvOptions;
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
		BdvOptions options = new BdvOptions();
		if(model.labeling().numDimensions() == 2
			|| (model.labeling().numDimensions() > 2
				&& model.labeling().dimension(2) == 1)) {
			options.is2D();
		}
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(context, options);
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
