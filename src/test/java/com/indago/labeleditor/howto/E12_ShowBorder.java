package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import com.indago.labeleditor.plugin.renderers.BorderLabelEditorRenderer;
import com.indago.labeleditor.core.view.LabelEditorView;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E12_ShowBorder {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel<Integer> panel;

	@Test
	@Ignore
	public void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		panel = new LabelEditorBdvPanel<Integer>() {
			@Override
			protected void addRenderings(LabelEditorView<Integer> renderingManager) {
				renderingManager.renderers().add(new BorderLabelEditorRenderer<>());
			}
		};
		panel.init(new ImgPlus<IntType>(input), labeling);


		panel.model().labels().getMapping().getLabels().forEach(label ->
				panel.model().tagging().addTag("displayed", label));
		panel.view().colors().put("displayed", ARGBType.rgba(255,255,0,55));
		panel.control().triggerLabelingChange();

		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(panel.get());
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
		new E12_ShowBorder().run();
	}

}
