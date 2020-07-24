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
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tags in the Labeleditor
 */
public class E06_RandomColors {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling, input);

		Random random = new Random();
		for (Integer label : labeling.getMapping().getLabels()) {
			System.out.println(label);
			// assign each label also as a tag to itself (so you can set colors for each label separately)
			model.tagging().addTagToLabel(label, label);
			// add random color to each tag
			model.colors().getFaceColor(label).set(random.nextInt(255), random.nextInt(255), random.nextInt(255), 200);
		}

		model.colors().getFocusFaceColor().set(255,255,0,255);
		model.colors().getSelectedFaceColor().set(0,255,255,255);

		ij.ui().show(model);
	}

	public static void main(String...args) throws IOException {
		new E06_RandomColors().run();
	}

}
