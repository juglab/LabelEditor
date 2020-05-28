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
package sc.fiji.labeleditor.howto.advanced;

import bdv.util.BdvOptions;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import sc.fiji.labeleditor.plugin.renderers.BorderLabelEditorRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to control which renderers will be used by the LabelEditor
 */
public class E05_ControlRenderers {


	/**
	 * By default, all renderers of annotated as plugins of type {@link LabelEditorRenderer} will be added to the viewer.
	 * You can control this and only add specific renderers / your own renderers.
	 */
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling, input);

		model.colors().getDefaultBorderColor().set(0,0,255);
		model.colors().getFocusBorderColor().set(0,255,0);
		model.colors().getSelectedBorderColor().set(255,0,0);

		// in this case, we do not try to find all existing renderers, but instead only add the border renderer
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(ij.context(), new BdvOptions().is2D()) {
			@Override
			public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model) {
				LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
				view.add(new BorderLabelEditorRenderer<>());
				return add(model, view);
			}
		};
		panel.add(model);

		JFrame frame = new JFrame("Label editor");
		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E05_ControlRenderers().run();
	}

}
