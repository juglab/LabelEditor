package sc.fiji.labeleditor.howto.basic;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.io.IOException;
import java.util.Random;

/**
 * How to mark specific labels with your own tags in the Labeleditor
 */
public class E06_RandomColors {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		ImgLabeling<Integer, IntType> labeling = ij.op().image().watershed(input, true, false);

		LabelEditorModel model = new DefaultLabelEditorModel<>(labeling, input);

		Random random = new Random();
		for (Integer label : labeling.getMapping().getLabels()) {
			System.out.println(label);
			// assign each label also as a tag to itself (so you can set colors for each label separately)
			model.tagging().addTagToLabel(label, label);
			// add random color to each tag
			model.colors().getBorderColor(label).set(random.nextInt(255), random.nextInt(255), random.nextInt(255), 200);
		}

		model.colors().getFocusFaceColor().set(255,255,0,255);
		model.colors().getSelectedFaceColor().set(0,255,255,255);

		ij.ui().show(model);
	}

	public static void main(String...args) throws IOException {
		new E06_RandomColors().run();
	}

}
