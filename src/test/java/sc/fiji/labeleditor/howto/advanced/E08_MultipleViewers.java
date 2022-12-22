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

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvStackSource;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E08_MultipleViewers {

	public <T extends RealType<T>> void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		RandomAccessibleInterval<T> input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		List<RandomAccessibleInterval<T>> inputStack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			inputStack.add(input);
		}
		input = ij.op().transform().stackView(inputStack);
		Img binary = (Img) ij.op().threshold().otsu(Views.iterable(input));
		ImgLabeling<Integer, IntType> labeling1 = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
		RandomAccessibleInterval gauss = ij.op().filter().gauss(input, 10);
		Img gaussBinary = (Img) ij.op().threshold().otsu(Views.iterable(gauss));
		ImgLabeling<Integer, IntType> labeling2 = ij.op().labeling().cca(gaussBinary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model1 = new DefaultLabelEditorModel<>(labeling1);
		DefaultLabelEditorModel<Integer> model2 = new DefaultLabelEditorModel<>(labeling2);

		model1.colors().getDefaultFaceColor().set(100,30,0,55);
		model2.colors().getDefaultFaceColor().set(0,55,55,55);

		BdvInterface bdvInterface1 = new BdvInterface(ij.context());
		BdvInterface bdvInterface2 = new BdvInterface(ij.context());

		JPanel viewer = new JPanel(new MigLayout());
		JFrame frame = new JFrame("Label editor");
		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		BdvHandlePanel panel1 = new BdvHandlePanel(frame, Bdv.options().accumulateProjectorFactory(bdvInterface1.projector()));
		BdvHandlePanel panel2 = new BdvHandlePanel(frame, Bdv.options().accumulateProjectorFactory(bdvInterface2.projector()));

		bdvInterface1.setup(panel1.getBdvHandle());
		bdvInterface1.control(model1);
		bdvInterface1.control(model2);

		bdvInterface2.setup(panel2.getBdvHandle());
		bdvInterface2.control(model1);
		bdvInterface2.control(model2);

		BdvStackSource<T> source = BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel1));
		source.setColor(new ARGBType(ARGBType.rgba(100, 100, 100,255)));
		source = BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel2));
		source.setColor(new ARGBType(ARGBType.rgba(100, 100, 100,255)));

		viewer.add( panel1.getViewerPanel(), "span, grow, push" );
		viewer.add( panel2.getViewerPanel(), "span, grow, push" );
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E08_MultipleViewers().run();
	}

}
