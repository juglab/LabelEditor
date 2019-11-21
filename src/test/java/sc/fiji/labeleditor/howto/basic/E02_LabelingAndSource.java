package sc.fiji.labeleditor.howto.basic;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.UIService;

import java.io.IOException;

/**
 * How to open a labeling together with it's data source in the LabelEditor
 */
public class E02_LabelingAndSource {

	/**
	 * You can  open an {@link ImgLabeling} with a matching source {@link Img} via {@link UIService) by creating a {@link LabelEditorModel}.
	 */
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel model = new DefaultLabelEditorModel(labeling);
		model.setData(input);

		ij.ui().show(model);
	}

	public static void main(String... args) throws IOException {
		new E02_LabelingAndSource().run();
	}

}
