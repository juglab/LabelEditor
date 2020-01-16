package sc.fiji.labeleditor.howto.advanced;

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
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
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;
import sc.fiji.labeleditor.plugin.interfaces.bvv.BvvInterface;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * How to display a labeling in an existing BDV instance
 */
public class E099_AddToExistingBVV {

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

//		JPanel viewer = new JPanel(new MigLayout());
//		JFrame frame = new JFrame("Label editor");
		BvvStackSource source = BvvFunctions.show(input, "raw", Bvv.options());
		BvvInterface.control(model1, source.getBvvHandle(), ij.context());
//
//		frame.setMinimumSize(new Dimension(500,500));
//		frame.setContentPane(viewer);
//		frame.pack();
//		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E099_AddToExistingBVV().run();
	}

}
