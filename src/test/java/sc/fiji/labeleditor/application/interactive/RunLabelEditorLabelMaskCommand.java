package sc.fiji.labeleditor.application.interactive;

import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.application.LabelEditorCommand;

import java.io.IOException;

public class RunLabelEditorLabelMaskCommand {

	public static void main(String...args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(RunLabelEditorLabelMaskCommand.class.getResource("/labelmap.png").getPath());
		RandomAccessibleInterval bg = ij.op().filter().gauss(input, 15);
		RandomAccessibleInterval stack = Views.stack(bg, input);
		ij.ui().show("labelmap", stack);
		ij.command().run(LabelEditorCommand.class, true);
	}

}
