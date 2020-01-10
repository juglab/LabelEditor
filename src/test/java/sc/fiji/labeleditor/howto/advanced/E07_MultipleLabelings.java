package sc.fiji.labeleditor.howto.advanced;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;
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
public class E07_MultipleLabelings {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling1 = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
		RandomAccessibleInterval gauss = ij.op().filter().gauss(input, 10);
		Img gaussBinary = (Img) ij.op().threshold().otsu(Views.iterable(gauss));
		ImgLabeling<Integer, IntType> labeling2 = ij.op().labeling().cca(gaussBinary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model1 = new DefaultLabelEditorModel<>(labeling1);
		DefaultLabelEditorModel<Integer> model2 = new DefaultLabelEditorModel<>(labeling2);

		model1.colors().getDefaultFaceColor().set(255,255,0,55);
		model2.colors().getDefaultFaceColor().set(255,0,255,55);

		JPanel viewer = new JPanel(new MigLayout());
		JFrame frame = new JFrame("Label editor");
		BdvHandlePanel panel = new BdvHandlePanel(frame, Bdv.options().is2D());

		BdvInterface.control(model1, panel.getBdvHandle());
		BdvInterface.control(model2, panel.getBdvHandle());
		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E07_MultipleLabelings().run();
	}

}
