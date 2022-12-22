/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
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

import bdv.util.*;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;
import sc.fiji.labeleditor.plugin.renderers.DefaultLabelEditorRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E01_AddToExistingBDV {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		BdvInterface labelEditorInterface = new BdvInterface(ij.context());
		BdvOptions options = BdvOptions.options().accumulateProjectorFactory(labelEditorInterface.projector()).is2D();
		BdvStackSource source = BdvFunctions.show(input, "raw", options);

		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		labelEditorInterface.setup(source.getBdvHandle());
		LabelEditorView<Integer> view = new DefaultLabelEditorView<>(model);
		view.add(new DefaultLabelEditorRenderer<>());
		labelEditorInterface.control(model, view);
		model.colors().getDefaultFaceColor().set(255,255,0,55);

		source.removeFromBdv();
	}

	public static void main(String...args) throws IOException {
		new E01_AddToExistingBDV().run();
	}

}
