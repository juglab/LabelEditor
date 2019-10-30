package com.indago.labeleditor.howto;


import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import io.scif.img.IO;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.IterableInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to open an {@link ImgLabeling} in a {@link LabelEditorBdvPanel}.
 */
public class E02_Open2DLabelingConflicts {

	@Test
	@Ignore
	public void run() {

		String LABEL1 = "label1";
		String LABEL2 = "label2";

		String TAG1 = "tag1";
		String TAG2 = "tag2";

		Img input = IO.openImgs(LabelEditorBdvPanel.class.getResource("/raw.tif").getPath()).get(0);
		ImgPlus data = new ImgPlus(input, "input", new AxisType[]{Axes.X, Axes.Y, Axes.TIME});

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1), data.dimension(2) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );


		Views.interval( labels, Intervals.createMinSize( 220, 220, 0, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL2 ) );

		Views.interval( labels, Intervals.createMinSize( 320, 320, 1, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 300, 300, 1, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL1 ) );

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		model.addTag(TAG1, LABEL1);
		model.addTag(TAG2, LABEL2);

		LabelEditorPanel<String> labelEditorPanel = new LabelEditorBdvPanel<>();
		labelEditorPanel.init(model);
		labelEditorPanel.getRenderer().setTagColor(TAG1, ARGBType.rgba(255,255,0,100));
		labelEditorPanel.getRenderer().setTagColor(TAG2, ARGBType.rgba(255,0,255,100));
		labelEditorPanel.updateLabelRendering();
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(labelEditorPanel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String... args) {
		new E02_Open2DLabelingConflicts().run();
	}

}
