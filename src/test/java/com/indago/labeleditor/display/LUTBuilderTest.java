package com.indago.labeleditor.display;

import com.indago.labeleditor.LabelEditorPanel;
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
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Before;
import org.junit.Test;

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
		LabelEditorPanel panel = new LabelEditorPanel<T, String>(data, labels) {
			@Override
			protected LUTBuilder<String> initLUTBuilder() {
				return (model) -> new int[]{0,1};
			}
		};
		LUTBuilder builder = panel.getLUTBuilder();
		assertNotNull(builder);
		int[] lut = builder.build(null);
		assertNotNull(lut);
		assertEquals(2, lut.length);
		assertEquals(0, lut[0]);
		assertEquals(1, lut[1]);
	}

}
