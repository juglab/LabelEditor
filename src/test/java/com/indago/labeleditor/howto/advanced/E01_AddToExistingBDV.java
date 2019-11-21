package com.indago.labeleditor.howto.advanced;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.interfaces.bdv.BdvInterface;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E01_AddToExistingBDV {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		model.colors().getDefaultFaceColor().set(255,255,0,55);

		LabelEditorView<Integer> view = new LabelEditorView<>(model);
		view.renderers().addDefaultRenderers();

		JPanel viewer = new JPanel(new MigLayout());
		JFrame frame = new JFrame("Label editor");
		BdvHandlePanel panel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));
		view.renderers().forEach(renderer -> BdvFunctions.show(renderer.getOutput(), renderer.getName(), Bdv.options().addTo(panel)));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		BdvInterface.control(model, view, panel);

		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E01_AddToExistingBDV().run();
	}

}
