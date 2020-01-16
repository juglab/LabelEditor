package sc.fiji.labeleditor.howto.basic;

import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.roi.labeling.ImgLabeling;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.IntType;

import java.io.IOException;

public class E05_SetTagsAndColors {

	public void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(
				binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		String TAG1 = "tag1";
		String TAG2 = "tag2";
		String TAG3 = "tag3";

		model.tagging().addTagToLabel(TAG1, 1);
		model.tagging().addTagToLabel(TAG1, 7);
		model.tagging().addTagToLabel(TAG1, 14);

		model.tagging().addTagToLabel(TAG2, 3);
		model.tagging().addTagToLabel(TAG2, 13);
		model.tagging().addTagToLabel(TAG2, 28);

		model.tagging().addTagToLabel(TAG3, 5);
		model.tagging().addTagToLabel(TAG3, 18);
		model.tagging().addTagToLabel(TAG3, 25);

		model.colors().getFaceColor(TAG1).set(255,50, 0);
		model.colors().getFaceColor(TAG2).set(0,50, 255);
		model.colors().getFaceColor(TAG3).set(50,255, 0);

		ij.ui().show(model);
	}

	public static void main(String...args) throws IOException {
		new E05_SetTagsAndColors().run();
	}

}
