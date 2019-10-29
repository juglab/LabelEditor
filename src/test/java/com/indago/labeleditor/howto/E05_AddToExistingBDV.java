package com.indago.labeleditor.howto;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.AbstractActionHandler;
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
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E05_AddToExistingBDV {

//	@Test
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		LabelEditorRenderer<Integer> renderer = new DefaultLabelEditorRenderer<>(model);
		model.getOrderedLabelRegions().forEach((label, regions) -> {
			model.addTag("displayed", label);
		});
		renderer.setTagColor("displayed", ARGBType.rgba(255,255,0,55));


		JFrame frame = new JFrame("Label editor");
		frame.setMinimumSize(new Dimension(500,500));
		JPanel viewer = new JPanel(new MigLayout());
		BdvHandlePanel bdvHandlePanel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(bdvHandlePanel));
		BdvFunctions.show(renderer.getRenderedLabels(), "labels", Bdv.options().addTo(bdvHandlePanel));

		viewer.add( bdvHandlePanel.getViewerPanel(), "span, grow, push" );
		ActionHandler actionHandler = new BdvActionHandler<>(bdvHandlePanel, model, renderer);
		actionHandler.init();

		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E05_AddToExistingBDV().run();
	}

}
