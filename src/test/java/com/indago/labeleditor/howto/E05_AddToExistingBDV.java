package com.indago.labeleditor.howto;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.plugin.interfaces.bdv.BdvInterface;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;
import org.junit.AfterClass;
import org.junit.Ignore;
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
	@Ignore
	public void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>();
		model.init(labeling);
		model.labels().getMapping().getLabels().forEach(label -> model.tagging().addTag("displayed", label));
		model.colors().get("displayed").put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255,255,0,55));

		LabelEditorView<Integer> view = new LabelEditorView<>(model);
		view.renderers().addDefaultRenderers();

		JPanel viewer = new JPanel(new MigLayout());
		panel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));
		view.renderers().forEach(renderer -> BdvFunctions.show(renderer.getOutput(), renderer.getName(), Bdv.options().addTo(panel)));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		BdvInterface.control(model, view, panel);

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
