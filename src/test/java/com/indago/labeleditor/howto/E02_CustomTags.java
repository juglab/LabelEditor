package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tabs in the {@link com.indago.labeleditor.LabelEditorPanel}.
 */
public class E02_CustomTags {

	@Test
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);
		LabelEditorPanel<Integer, IntType> labelEditorPanel = new LabelEditorPanel<>(new ImgPlus<>(input), labeling);

		Random random = new Random();
		for (LabelingType<Integer> labels : labeling) {
			for (Integer label : labels) {
				labelEditorPanel.getModel().addTag(label, label);
				labelEditorPanel.getRenderer().setTagColor(label, ARGBType.rgba(random.nextFloat()*255, random.nextFloat()*255, random.nextFloat()*255, 150));

			}
		}

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(labelEditorPanel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E02_CustomTags().run();
	}

}
