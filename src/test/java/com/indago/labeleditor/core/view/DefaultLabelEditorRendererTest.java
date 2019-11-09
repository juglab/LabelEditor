package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.plugin.renderer.DefaultLabelEditorRenderer;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTag("b", "b");
		LabelEditorView<String> view = new LabelEditorView<>(model);
		int red = ARGBType.rgba(255, 0, 0, 100);
		view.colors().put("b", red);
		view.updateOnTagChange();
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
		assertEquals(LabelEditorView.colorDefault, outRa.get().get());
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

	private void printColor(ARGBType argbType) {
		System.out.println(ARGBType.red(argbType.get()) + ", " + ARGBType.green(argbType.get()) + ", " + ARGBType.blue(argbType.get()) + ", " + ARGBType.alpha(argbType.get()));
	}

	@Test
	public void testMixColors() {
		Map<Object, LUTChannel> tagColors = new HashMap<>();
		String tag1 = "tag1";
		String tag2 = "tag2";
		tagColors.put(tag1, new LUTChannel(ARGBType.rgba(255, 0, 0, 100)));
		tagColors.put(tag2, new LUTChannel(ARGBType.rgba(0, 255, 0, 100)));
		HashSet<Object> noSet = new HashSet<>();
		int colorNoTag = DefaultLabelEditorRenderer.mixColors(noSet, tagColors);
		assertEquals(0, ARGBType.red(colorNoTag));
		assertEquals(0, ARGBType.green(colorNoTag));
		assertEquals(0, ARGBType.blue(colorNoTag));
		assertEquals(0, ARGBType.alpha(colorNoTag));
		HashSet<Object> tag1Set = new HashSet<>();
		tag1Set.add(tag1);
		int colorTag1 = DefaultLabelEditorRenderer.mixColors(tag1Set, tagColors);
		assertEquals(255, ARGBType.red(colorTag1));
		assertEquals(0, ARGBType.green(colorTag1));
		assertEquals(0, ARGBType.blue(colorTag1));
		assertEquals(100, ARGBType.alpha(colorTag1));
		//TODO test second color and mixed color
	}

}
