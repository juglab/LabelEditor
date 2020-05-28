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
import sc.fiji.labeleditor.plugin.behaviours.select.ConflictSelectionBehaviours;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;

import javax.swing.*;
import java.awt.*;

/**
 * How to use the conflict selection mode
 */
public class E06_HandleSegmentationConflicts {

	/**
	 * In conflict selection mode only labels which are in conflict with your newly selected label will be deselected.
	 */
	public void run() {

		ImageJ ij = new ImageJ();
		ij.launch();

		String LABEL1 = "label1";
		String LABEL2 = "label2";
		String LABEL3 = "label3";
		String LABEL4 = "label4";
		String LABEL5 = "label5";
		String LABEL6 = "label6";
		String LABEL7 = "label7";
		String LABEL8 = "label8";
		String LABEL9 = "label9";

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 500, 500 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		int radius = 50;
		drawSphere(labels, new long[]{170, 170}, radius, LABEL1);
		drawSphere(labels, new long[]{170, 200}, radius, LABEL2);
		drawSphere(labels, new long[]{200, 200}, radius, LABEL3);
		drawSphere(labels, new long[]{200, 170}, radius, LABEL4);

		drawSphere(labels, new long[]{240, 280}, (int) (radius*1.2), LABEL9);

		drawSphere(labels, new long[]{270, 370}, radius, LABEL5);
		drawSphere(labels, new long[]{270, 400}, radius, LABEL6);
		drawSphere(labels, new long[]{300, 400}, radius, LABEL7);
		drawSphere(labels, new long[]{300, 370}, radius, LABEL8);
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL2);
		model.tagging().addTagToLabel(LabelEditorTag.SELECTED, LABEL7);

		model.colors().getSelectedBorderColor().set(0,255,255,100);
		model.colors().getDefaultFaceColor().set(0,0,0,0);
		model.colors().getDefaultBorderColor().set(0,255,255,100);

		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(ij.context(), new BdvOptions().is2D());

		InteractiveLabeling<String> interactiveLabeling = panel.add(model);
		interactiveLabeling.interfaceInstance().install(new ConflictSelectionBehaviours<>(), interactiveLabeling);

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	private void drawSphere(ImgLabeling<String, IntType> img, long[] position, int radius, String label) {
		RandomAccess<LabelingType<String>> ra = img.randomAccess();
		ra.setPosition(position);
		HyperSphere<LabelingType<String>> hyperSphere = new HyperSphere<>(img, ra, radius);
		for (LabelingType<String> value : hyperSphere)
			value.add(label);
	}

	public static void main(String... args) {
		new E06_HandleSegmentationConflicts().run();
	}

}
