package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tabs in the {@link LabelEditorBdvPanel}.
 */
public class E07_CustomTags {

	@Test
	@Ignore
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);
		LabelEditorPanel<Integer> labelEditorPanel = new LabelEditorBdvPanel<>();
		labelEditorPanel.init(labeling);

		Random random = new Random();
		for (LabelingType<Integer> labels : labeling) {
			for (Integer label : labels) {
				labelEditorPanel.getModel().addTag(label, label);
				int brightness = random.nextInt(155);
				labelEditorPanel.getRenderer().setTagColor(label, ARGBType.rgba(brightness, brightness, brightness, 150));

			}
		}
		labelEditorPanel.getRenderer().setTagColor(LabelEditorTag.MOUSE_OVER, ARGBType.rgba(255,255,0,255));
		labelEditorPanel.getRenderer().setTagColor(LabelEditorTag.SELECTED, ARGBType.rgba(0,255,255,255));
		labelEditorPanel.updateLabelRendering();

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(labelEditorPanel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E07_CustomTags().run();
	}

}
