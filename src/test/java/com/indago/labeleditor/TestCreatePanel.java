package com.indago.labeleditor;

import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.VisibleTag;
import io.scif.img.IO;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

public class TestCreatePanel {

	@Test
	public <T extends RealType<T> & NativeType<T>> void run() {
		Img input = IO.openImgs(LabelEditorPanel.class.getResource("/raw.tif").getPath()).get(0);
		ImgPlus<T> data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y, Axes.TIME});

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";
		String LABEL2 = "label2";
		VisibleTag visibleTag = new VisibleTag();

		Views.interval( labels, Intervals.createMinSize( 20, 20, 100, 100 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 80, 80, 100, 100 ) ).forEach( pixel -> pixel.add( LABEL2 ) );

		DefaultLabelEditorModel<T, String> model = new DefaultLabelEditorModel<>(data, labels);

		model.addTag(0, LABEL1, visibleTag);

		LabelEditorPanel<T, String> labelEditorPanel = new LabelEditorPanel<>(model);
	}

}
