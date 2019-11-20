package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tabs in the {@link LabelEditorBdvPanel}.
 */
public class E07_CustomTags {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);
		LabelEditorPanel<Integer> panel = new LabelEditorBdvPanel<>();
		ij.context().inject(panel);
		panel.init(labeling, new ImgPlus(input));

		Random random = new Random();
		panel.model().tagging().pauseListeners();
		panel.model().colors().pauseListeners();

		for (Integer label : labeling.getMapping().getLabels()) {
			System.out.println(label);
			panel.model().tagging().addTag(label, label);
//				int brightness = random.nextInt(155);
//				panel.view().colors().get(label).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(brightness, brightness, brightness, 255));
			panel.model().colors().getBorderColor(label).set(random.nextInt(255), random.nextInt(255), random.nextInt(255), 200);

		}
		panel.model().tagging().resumeListeners();
		panel.model().colors().resumeListeners();
		panel.model().colors().getFocusFaceColor().set(255,255,0,25);
		panel.model().colors().getSelectedFaceColor().set(0,255,255,255);

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E07_CustomTags().run();
	}

}
