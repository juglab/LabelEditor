package com.indago.labeleditor.display;

import com.indago.labeleditor.DefaultLabelEditorRenderer;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.LabelEditorRenderer;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
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

public class LUTBuilderTest <T extends RealType<T> & NativeType<T>> {

	private ImgPlus<T> data;
	private ImgLabeling<String, IntType> labels;

	@Before
	public void initData() {
		Img input = new ArrayImgFactory<>(new IntType()).create(10, 10);
		data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y});
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		labels = new ImgLabeling<>( backing );
	}


	@Test
	public void overrideLUTBuilder() {
		LabelEditorRenderer renderer = new DefaultLabelEditorRenderer<String>(null) {
			@Override
			public void update() {
				lut = new int[]{0,1};
			}
		};
		renderer.update();
		int[] lut = renderer.getLUT();
		assertNotNull(lut);
		assertEquals(2, lut.length);
		assertEquals(0, lut[0]);
		assertEquals(1, lut[1]);
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
