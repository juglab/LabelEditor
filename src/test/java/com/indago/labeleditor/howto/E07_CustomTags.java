package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tabs in the {@link LabelEditorBdvPanel}.
 */
public class E07_CustomTags {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel<Integer> panel;

	@Test
	public void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);
		panel = new LabelEditorBdvPanel<>();
		panel.init(new ImgPlus(input), labeling);

		Random random = new Random();
		for (LabelingType<Integer> labels : labeling) {
			for (Integer label : labels) {
				panel.model().tagging().addTag(label, label);
				int brightness = random.nextInt(155);
				panel.rendering().setTagColor(label, ARGBType.rgba(random.nextInt(255), random.nextInt(255), random.nextInt(255), 255));

			}
		}
		panel.rendering().setTagColor(LabelEditorTag.MOUSE_OVER, ARGBType.rgba(255,255,0,255));
		panel.rendering().setTagColor(LabelEditorTag.SELECTED, ARGBType.rgba(0,255,255,255));
		panel.updateLabelRendering();

		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	@AfterClass
	public static void dispose() {
		ij.context().dispose();
		frame.dispose();
		panel.dispose();
	}

	public static void main(String...args) throws IOException {
		new E07_CustomTags().run();
	}

}
