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
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.ClickBehaviour;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to add custom behaviours to the LabelEditor
 */
public class E02_CustomActions {

	/**
	 * In this example, the default selection and focus colors are removed from the model.
	 * One can toggle a special tag of a label where the mouse is currently hovering by pressing L on the keyboard.
	 */
	public void mouseAction() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		//open blobs
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		//do connected component analysis
		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		//create model
		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling, input);

		//set colors
		model.colors().getColorset(LabelEditorTag.SELECTED).clear();
		model.colors().getColorset(LabelEditorTag.MOUSE_OVER).clear();
		model.colors().getFaceColor("special").set(255, 0, 0);

		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(ij.context(), new BdvOptions().is2D());
		InteractiveLabeling<Integer> interactiveLabeling = panel.add(model);

//		panel.getSources().forEach(source -> source.setDisplayRange(0, 100));

		interactiveLabeling.interfaceInstance().behaviours(interactiveLabeling).behaviour((ClickBehaviour) (x, y) -> {

				//get labels at current mouse position
				LabelingType<Integer> labels = interactiveLabeling.interfaceInstance().findLabelsAtMousePosition(x, y, interactiveLabeling);

				//pausing the tagging listeners while changing tags improves performance
				model.tagging().pauseListeners();
				// for all labels at the current mouse position, toggle special tag
				for (Integer label : labels) {
					model.tagging().toggleTag("special", label);
				}
				model.tagging().resumeListeners();
			},
		"add my special label","L" );

		//build frame
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E02_CustomActions().mouseAction();
	}

}
