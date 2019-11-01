package com.indago.labeleditor.howto;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.BdvActionHandler;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E05_AddToExistingBDV {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static BdvHandlePanel panel;

	@Test
	public void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		LabelEditorRenderer<Integer> renderer = new DefaultLabelEditorRenderer<>(model);
		model.labelRegions().forEach((label, regions) -> {
			model.tagging().addTag("displayed", label);
		});
		renderer.setTagColor("displayed", ARGBType.rgba(255,255,0,55));

		JPanel viewer = new JPanel(new MigLayout());
		panel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));
		BdvFunctions.show(renderer.getRenderedLabels(), "labels", Bdv.options().addTo(panel));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		ActionHandler actionHandler = new BdvActionHandler<>(panel, model, renderer);
		actionHandler.init();

		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	@AfterClass
	public static void dispose() {
		ij.context().dispose();
		frame.dispose();
		panel.close();
	}

	public static void main(String...args) throws IOException {
		new E05_AddToExistingBDV().run();
	}

}
