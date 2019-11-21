package sc.fiji.labeleditor.howto.basic;

import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.UIService;

import java.io.IOException;

/**
 * How to open a labeling in the LabelEditor via {@link UIService)
 */
public class E01_2DLabeling {

	public void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);

		ImgLabeling labeling = ij.op().labeling().cca(
				binary,
				ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		ij.ui().show(labeling);

	}

	public static void main(String... args) throws IOException {
		new E01_2DLabeling().run();
	}

}
