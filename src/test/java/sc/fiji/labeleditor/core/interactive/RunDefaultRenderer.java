package sc.fiji.labeleditor.core.interactive;

import bdv.util.BdvFunctions;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorRenderers;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.renderers.DefaultLabelEditorRenderer;

import static org.junit.Assert.assertEquals;

public class RunDefaultRenderer {

	public static <T> void main(String...args) {
		ImgPlus<T> data;
		ImgLabeling<String, IntType> labels;
		Img input = new ArrayImgFactory<>(new IntType()).create(2, 2);
		data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y});
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		labels = new ImgLabeling<>( backing );
		RandomAccess<LabelingType<String>> ra = labels.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("a");
		ra.setPosition(new long[]{0,1});
		ra.get().add("b");
		ra.setPosition(new long[]{1,0});
		ra.get().add("a");
		ra.get().add("b");
		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTagToLabel("b", "b");
		int red = ARGBType.rgba(255, 0, 0, 100);
		model.colors().getFaceColor("b").set(red);
		LabelEditorView<String> view = new DefaultLabelEditorView<>(model);
		view.renderers().add(new DefaultLabelEditorRenderer<>());
		LabelEditorRenderers renderers = view.renderers();
		assertEquals(1, renderers.size());
		RandomAccessibleInterval<ARGBType> rendering = renderers.get(0).getOutput();

		BdvFunctions.show(rendering, "");
	}

}
