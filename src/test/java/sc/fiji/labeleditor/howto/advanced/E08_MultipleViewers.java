package sc.fiji.labeleditor.howto.advanced;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E08_MultipleViewers {

	public <T extends RealType<T>> void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		RandomAccessibleInterval<T> input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		List<RandomAccessibleInterval<T>> inputStack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			inputStack.add(input);
		}
		input = ij.op().transform().stackView(inputStack);
		Img binary = (Img) ij.op().threshold().otsu(Views.iterable(input));
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
		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		BdvHandlePanel panel1 = new BdvHandlePanel(frame, Bdv.options());
		BdvHandlePanel panel2 = new BdvHandlePanel(frame, Bdv.options());

		BdvInterface.control(model1, panel1.getBdvHandle(), ij.context());
		BdvInterface.control(model2, panel1.getBdvHandle(), ij.context());
		BdvInterface.control(model1, panel2.getBdvHandle(), ij.context());
		BdvInterface.control(model2, panel2.getBdvHandle(), ij.context());
		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel1));
		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel2));

		viewer.add( panel1.getViewerPanel(), "span, grow, push" );
		viewer.add( panel2.getViewerPanel(), "span, grow, push" );
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E08_MultipleViewers().run();
	}

}
