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
package sc.fiji.labeleditor.howto.basic;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.UIService;

import java.io.IOException;

/**
 * How to open a labeling together with it's data source in the LabelEditor
 */
public class E03_ChangeDefaultColors {

	/**
	 * You can  open an {@link ImgLabeling} with a matching source {@link Img} via {@link UIService) by creating a {@link LabelEditorModel}.
	 */
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = ij.scifio().datasetIO().open(getClass().getResource("/blobs.png").getPath());
		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel model = new DefaultLabelEditorModel<>(labeling, input);

		model.colors().getDefaultBorderColor().set(0, 255, 255);
		model.colors().getDefaultFaceColor().set(0,0,0,0);
		model.colors().getSelectedBorderColor().set(0,255,0);
		model.colors().getSelectedFaceColor().set(255,255,0,100);
		model.colors().getFocusBorderColor().set(255,0,0);
		model.colors().getFocusFaceColor().set(0,0,0,0);

		ij.ui().show(model);
	}

	public static void main(String... args) throws IOException {
		new E03_ChangeDefaultColors().run();
	}

}
