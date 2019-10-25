package com.indago.labeleditor.interactive;

import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
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

import javax.swing.*;
import java.awt.*;

public class RunLabelEditorPanel {
	public static void main(String... args) {
		DefaultLabelEditorModel model = buildModel();
		JFrame frame = new JFrame("Label editor");
		JPanel parent = new JPanel();
		frame.setContentPane(parent);
		frame.setMinimumSize(new Dimension(500,500));
		LabelEditorPanel labelEditorPanel = new LabelEditorPanel<>(model);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.add(labelEditorPanel);
		frame.pack();
		frame.setVisible(true);
	}

	private static <T extends RealType<T> & NativeType<T>> DefaultLabelEditorModel<T, String> buildModel() {
		Img input = IO.openImgs(LabelEditorPanel.class.getResource("/raw.tif").getPath()).get(0);
		ImgPlus<T> data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y, Axes.TIME});

		String LABEL1 = "label1";
		String LABEL2 = "label2";

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1), data.dimension(2) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		Views.interval( labels, Intervals.createMinSize( 20, 20, 0, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 80, 80, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL2 ) );

		Views.interval( labels, Intervals.createMinSize( 120, 120, 1, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 180, 180, 1, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL1 ) );

		return new DefaultLabelEditorModel<>(data, labels);
	}
}
