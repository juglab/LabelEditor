package sc.fiji.labeleditor.plugin.renderers;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorRenderers;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import sc.fiji.labeleditor.core.view.LabelEditorView;
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
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DefaultLabelEditorRendererTest<T extends RealType<T> & NativeType<T>> {

	private ImgPlus<T> data;
	private ImgLabeling<String, IntType> labels;

	@Before
	public void initData() {
		Img input = new ArrayImgFactory<>(new IntType()).create(2, 2);
		data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y});
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		labels = new ImgLabeling<>( backing );
	}

	@Test
	public void testDefaultRendering() {
		RandomAccess<LabelingType<String>> ra = labels.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("a");
		ra.setPosition(new long[]{0,1});
		ra.get().add("b");
		ra.setPosition(new long[]{1,0});
		ra.get().add("a");
		ra.get().add("b");
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTagToLabel("b", "b");
		int red = ARGBType.rgba(255, 0, 0, 255);
		model.colors().getFaceColor("b").set(red);
		int green = ARGBType.rgba(0, 255, 0, 255);
		model.colors().getDefaultFaceColor().set(green);

		LabelEditorView<String> view = new LabelEditorView<>(model);
		view.renderers().add(new DefaultLabelEditorRenderer<>());
		LabelEditorRenderers renderings = view.renderers();
		assertEquals(1, renderings.size());
		((DefaultLabelEditorRenderer)renderings.get(0)).printLUT();
		RandomAccessibleInterval<ARGBType> rendering = renderings.get(0).getOutput();
		assertNotNull(rendering);
		assertEquals(data.numDimensions(), rendering.numDimensions());
		assertEquals(data.dimension(0), rendering.dimension(0));
		assertEquals(data.dimension(1), rendering.dimension(1));
		RandomAccess<ARGBType> outRa = rendering.randomAccess();
		outRa.setPosition(new long[]{0,0}); // labels {a}
		printColor(outRa.get());
		assertEquals(green, outRa.get().get());
		outRa.setPosition(new long[]{0,1}); // labels {b}
		printColor(outRa.get());
		assertEquals(red, outRa.get().get());
		outRa.setPosition(new long[]{1,0}); // labels {a,b}
		printColor(outRa.get());
		assertEquals(red, outRa.get().get());
		outRa.setPosition(new long[]{1,1}); // labels {}
		printColor(outRa.get());
		assertEquals(0, outRa.get().get());
	}

	@Test
	public void testDefaultRenderingMixColors() {
		//img
		RandomAccess<LabelingType<String>> ra = labels.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("a");
		ra.setPosition(new long[]{0,1});
		ra.get().add("b");
		ra.setPosition(new long[]{1,0});
		ra.get().add("a");
		ra.get().add("b");

		//model
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTagToLabel("b", "b");
		model.tagging().addTagToLabel("a", "a");
		int red = ARGBType.rgba(255, 0, 0, 100);
		int transparent = ARGBType.rgba(0, 0, 0, 0);
		model.colors().getFaceColor("a").set(transparent);
		model.colors().getFaceColor("b").set(red);
		model.colors().getDefaultFaceColor().set(0);

		//view
		LabelEditorView<String> view = new LabelEditorView<>(model);
		view.renderers().add(new DefaultLabelEditorRenderer<>());
		LabelEditorRenderers renderings = view.renderers();
		assertEquals(1, renderings.size());
		RandomAccessibleInterval<ARGBType> rendering = renderings.get(0).getOutput();
		assertNotNull(rendering);
		assertEquals(data.numDimensions(), rendering.numDimensions());
		assertEquals(data.dimension(0), rendering.dimension(0));
		assertEquals(data.dimension(1), rendering.dimension(1));
		RandomAccess<ARGBType> outRa = rendering.randomAccess();
		outRa.setPosition(new long[]{0,0}); // labels {a}
		printColor(outRa.get());
		assertEquals(transparent, outRa.get().get());
		outRa.setPosition(new long[]{0,1}); // labels {b}
		printColor(outRa.get());
		assertEquals(red, outRa.get().get());
		outRa.setPosition(new long[]{1,0}); // labels {a,b}
		printColor(outRa.get());
		assertEquals(red, outRa.get().get());
		outRa.setPosition(new long[]{1,1}); // labels {}
		printColor(outRa.get());
		assertEquals(0, outRa.get().get());
	}


	@Test
	public void testDefaultRenderingAlphaBlending() {
		RandomAccess<LabelingType<String>> ra = labels.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("a");
		ra.setPosition(new long[]{0,1});
		ra.get().add("b");
		ra.setPosition(new long[]{1,0});
		ra.get().add("a");
		ra.get().add("b");

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTagToLabel("a", "b");
		model.tagging().addTagToLabel("a", "a");
		int color = ARGBType.rgba(255, 255, 255, 100);
		int mixedColor = ARGBType.rgba(255, 255, 255, 160);
		model.colors().getFaceColor("a").set(color);
		model.colors().getDefaultFaceColor().set(0);

		LabelEditorView<String> view = new LabelEditorView<>(model);
		view.renderers().add(new DefaultLabelEditorRenderer<>());
		LabelEditorRenderers renderings = view.renderers();
		RandomAccessibleInterval<ARGBType> rendering = renderings.get(0).getOutput();
		RandomAccess<ARGBType> outRa = rendering.randomAccess();
		outRa.setPosition(new long[]{0,0}); // labels {a}
		assertEquals(color, outRa.get().get());
		outRa.setPosition(new long[]{0,1}); // labels {b}
		assertEquals(color, outRa.get().get());
		outRa.setPosition(new long[]{1,0}); // labels {a,b}
		assertEquals(mixedColor, outRa.get().get());
		outRa.setPosition(new long[]{1,1}); // labels {}
		assertEquals(0, outRa.get().get());
	}


	private void printColor(ARGBType argbType) {
		System.out.println(ARGBType.red(argbType.get()) + ", " + ARGBType.green(argbType.get()) + ", " + ARGBType.blue(argbType.get()) + ", " + ARGBType.alpha(argbType.get()));
	}

	@Test
	public void testMixColors() {
		LabelEditorTagColors tagColors = new LabelEditorTagColors();
		String tag1 = "tag1";
		String tag2 = "tag2";
		tagColors.getFaceColor(tag1).set(255, 0, 0, 100);
		tagColors.getFaceColor(tag2).set(0, 255, 0, 100);
		List<Object> noSet = new ArrayList<>();
		int colorNoTag = DefaultLabelEditorRenderer.mixColors(noSet, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(0, ARGBType.red(colorNoTag));
		assertEquals(0, ARGBType.green(colorNoTag));
		assertEquals(0, ARGBType.blue(colorNoTag));
		assertEquals(0, ARGBType.alpha(colorNoTag));
		List<Object> tag1Set = new ArrayList<>();
		tag1Set.add(tag1);
		int colorTag1 = DefaultLabelEditorRenderer.mixColors(tag1Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(255, ARGBType.red(colorTag1));
		assertEquals(0, ARGBType.green(colorTag1));
		assertEquals(0, ARGBType.blue(colorTag1));
		assertEquals(100, ARGBType.alpha(colorTag1));
		//TODO test second color and mixed color
	}

	@Test
	public void testMixTransparentColors() {
		LabelEditorTagColors tagColors = new LabelEditorTagColors();
		String tag1 = "tag1";
		String tag2 = "tag2";
		tagColors.getFaceColor(tag1).set(0, 0, 0, 0);
		tagColors.getFaceColor(tag2).set(255, 155, 0, 100);
		List<Object> noSet = new ArrayList<>();
		int colorNoTag = DefaultLabelEditorRenderer.mixColors(noSet, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(0, ARGBType.red(colorNoTag));
		assertEquals(0, ARGBType.green(colorNoTag));
		assertEquals(0, ARGBType.blue(colorNoTag));
		assertEquals(0, ARGBType.alpha(colorNoTag));
		List<Object> tag1Set = new ArrayList<>();
		tag1Set.add(tag1);
		int colorTag1 = DefaultLabelEditorRenderer.mixColors(tag1Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(0, ARGBType.red(colorTag1));
		assertEquals(0, ARGBType.green(colorTag1));
		assertEquals(0, ARGBType.blue(colorTag1));
		assertEquals(0, ARGBType.alpha(colorTag1));
		List<Object> tag2Set = new ArrayList<>();
		tag2Set.add(tag2);
		int colorTag2 = DefaultLabelEditorRenderer.mixColors(tag2Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(255, ARGBType.red(colorTag2));
		assertEquals(155, ARGBType.green(colorTag2));
		assertEquals(0, ARGBType.blue(colorTag2));
		assertEquals(100, ARGBType.alpha(colorTag2));
		List<Object> tag12Set = new ArrayList<>();
		tag12Set.add(tag1);
		tag12Set.add(tag2);
		int colorTag12 = DefaultLabelEditorRenderer.mixColors(tag12Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(255, ARGBType.red(colorTag12));
		assertEquals(155, ARGBType.green(colorTag12));
		assertEquals(0, ARGBType.blue(colorTag12));
		assertEquals(100, ARGBType.alpha(colorTag12));
		List<Object> tag21Set = new ArrayList<>();
		tag21Set.add(tag2);
		tag21Set.add(tag1);
		int colorTag21 = DefaultLabelEditorRenderer.mixColors(tag21Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(255, ARGBType.red(colorTag21));
		assertEquals(155, ARGBType.green(colorTag21));
		assertEquals(0, ARGBType.blue(colorTag21));
		assertEquals(100, ARGBType.alpha(colorTag21));
	}

	@Test
	public void testMixSameColors() {
		LabelEditorTagColors tagColors = new LabelEditorTagColors();
		String tag1 = "tag1";
		String tag2 = "tag2";
		tagColors.getFaceColor(tag1).set(255, 255, 255, 100);
		tagColors.getFaceColor(tag2).set(255, 255, 255, 100);
		List<Object> noSet = new ArrayList<>();
		int colorNoTag = DefaultLabelEditorRenderer.mixColors(noSet, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(0, ARGBType.red(colorNoTag));
		assertEquals(0, ARGBType.green(colorNoTag));
		assertEquals(0, ARGBType.blue(colorNoTag));
		assertEquals(0, ARGBType.alpha(colorNoTag));
		List<Object> tag12Set = new ArrayList<>();
		tag12Set.add(tag1);
		tag12Set.add(tag2);
		int colorTag12 = DefaultLabelEditorRenderer.mixColors(tag12Set, tagColors, LabelEditorTargetComponent.FACE);
		assertEquals(255, ARGBType.red(colorTag12));
		assertEquals(255, ARGBType.green(colorTag12));
		assertEquals(255, ARGBType.blue(colorTag12));
		assertEquals(160, ARGBType.alpha(colorTag12));
	}
}
