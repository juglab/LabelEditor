package sc.fiji.labeleditor.howto.basic;

import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorValueColor;

import java.io.IOException;

/**
 * How to assign value tags in the LabelEditor
 */
public class E08_ValueTags {

	public void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		Img<IntType> binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling, input);

		LabelRegions<Integer> regions = new LabelRegions<>(labeling);
		for (Integer label : labeling.getMapping().getLabels()) {
			//add the size of the label region as a value to the label
			LabelRegion<Integer> region = regions.getLabelRegion(label);
			model.tagging().addValueToLabel("size", new IntType((int) region.size()), label);
		}

		// create a color and the min / max values of this tag
		LabelEditorValueColor<IntType> indexColor = model.colors().makeValueFaceColor("size", new IntType(0), new IntType(800));
		indexColor.setMinColor(0,0,0,0);
		indexColor.setMaxColor(0,255,0,255);

		model.colors().getSelectedFaceColor().set(255,255,255);

		ij.ui().show(model);
	}

	public static void main(String... args) throws IOException {
		new E08_ValueTags().run();
	}

}
