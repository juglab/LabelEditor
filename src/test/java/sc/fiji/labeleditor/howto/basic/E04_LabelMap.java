package sc.fiji.labeleditor.howto.basic;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.img.Img;

import java.io.IOException;

public class E04_LabelMap {

	public void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());

		LabelEditorModel model = DefaultLabelEditorModel.initFromLabelMap(input);

		ij.ui().show(model);
	}

	public static void main(String...args) throws IOException {
		new E04_LabelMap().run();
	}

}
