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

import bdv.util.BdvOptions;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

/**
 * How to make the LabelEditor display a new labeling
 */
public class E03_ChangingInput {

	public void run() throws InterruptedException {

		ImageJ ij = new ImageJ();
		ij.launch();
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(100, 100);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		ImgPlus<IntType> imgPlus = new ImgPlus<>(img);
		for (int i = 0; i < 13; i++) {
			drawRandomSphere(imgPlus, ra, random);
		}

		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(ij.context(), new BdvOptions().is2D());

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);

		for (int i = 0; i < 1300; i++) {
			drawRandomSphere(imgPlus, ra, random);
			ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(imgPlus, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
			panel.removeModels();
			panel.add(new DefaultLabelEditorModel<>(labeling, imgPlus));
			Thread.sleep(3000);
		}
	}

	private void drawRandomSphere(Img<IntType> img, RandomAccess<IntType> ra, Random random) {
		ra.setPosition(new int[]{random.nextInt(100), random.nextInt(100)});
		HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
		for (IntType value : hyperSphere)
			try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException ignored) {}
	}

	public static void main(String... args) throws InterruptedException {
		new E03_ChangingInput().run();
	}
}
