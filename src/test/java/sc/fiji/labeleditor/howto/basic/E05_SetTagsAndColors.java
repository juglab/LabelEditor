package sc.fiji.labeleditor.howto.basic;

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

		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());

		LabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(input);

		String TAG1 = "tag1";
		String TAG2 = "tag2";
		String TAG3 = "tag3";

		model.tagging().addTagToLabel(TAG1, new IntType(1));
		model.tagging().addTagToLabel(TAG1, new IntType(7));
		model.tagging().addTagToLabel(TAG1, new IntType(14));

		model.tagging().addTagToLabel(TAG2, new IntType(3));
		model.tagging().addTagToLabel(TAG2, new IntType(13));
		model.tagging().addTagToLabel(TAG2, new IntType(28));

		model.tagging().addTagToLabel(TAG3, new IntType(5));
		model.tagging().addTagToLabel(TAG3, new IntType(18));
		model.tagging().addTagToLabel(TAG3, new IntType(25));

		model.colors().getFaceColor(TAG1).set(255,50, 0);
		model.colors().getFaceColor(TAG2).set(0,50, 255);
		model.colors().getFaceColor(TAG3).set(50,255, 0);

		ij.ui().show(model);
	}

	public static void main(String...args) throws IOException {
		new E05_SetTagsAndColors().run();
	}

}
