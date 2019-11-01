package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;

public class TestCreatePanel {

	private ImgPlus data;
	private ImgLabeling<String, IntType> labels;

	@Before
	public void initData() {
		Img input = new ArrayImgFactory<>(new IntType()).create(10, 10);
		data = new ImgPlus<>(input, "input", new AxisType[]{Axes.X, Axes.Y});
		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		labels = new ImgLabeling<>( backing );
	}

	@Test
	public void run() {
		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		LabelEditorBdvPanel<String> panel = new LabelEditorBdvPanel<>();
		panel.init(data, model);
	}

	@Test
	public void useEmptyConstructor() {
		LabelEditorBdvPanel<String> panel = new LabelEditorBdvPanel<>();
		panel.init(data, labels);
		panel.dispose();
	}

	@Test
	public void showOnlyData() {
		LabelEditorBdvPanel<Object> panel = new LabelEditorBdvPanel<>();
		panel.init(data);
		panel.dispose();
	}

}
