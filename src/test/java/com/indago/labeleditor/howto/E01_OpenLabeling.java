package com.indago.labeleditor.howto;


import com.indago.labeleditor.LabelEditorPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to open an {@link net.imglib2.roi.labeling.ImgLabeling} in a {@link com.indago.labeleditor.LabelEditorPanel}.
 */
public class E01_OpenLabeling {

	@Test
	public void watershedExample() throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
		LabelEditorPanel labelEditorPanel = new LabelEditorPanel<>(new ImgPlus(input), labeling);
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(labelEditorPanel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String... args) throws IOException {
		new E01_OpenLabeling().watershedExample();
	}

}
