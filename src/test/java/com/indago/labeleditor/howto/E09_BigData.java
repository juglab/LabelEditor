package com.indago.labeleditor.howto;

import com.indago.labeleditor.AbstractLabelEditorPanel;
import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorBvvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class E09_BigData {

	@Test
	public void run() {

		DiskCachedCellImg<IntType, ?> backing = new DiskCachedCellImgFactory<>(new IntType()).create( 500, 500, 500 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		String LABEL1 = "label1";
		String LABEL2 = "label2";

		String TAG1 = "tag1";
		String TAG2 = "tag2";

		Random random = new Random();

		for (int i = 0; i < 10; i++) {
			try {
				Views.interval( labels,
								Intervals.createMinSize(
										random.nextInt((int) backing.dimension(0)),
										random.nextInt((int) backing.dimension(1)),
										random.nextInt((int) backing.dimension(2)),
										100, 100, 100 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
			} catch(ArrayIndexOutOfBoundsException ignored) {}
			try {
				Views.interval( labels,
						Intervals.createMinSize(
								random.nextInt((int) backing.dimension(0)),
								random.nextInt((int) backing.dimension(1)),
								random.nextInt((int) backing.dimension(2)),
								100, 100, 100 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
			} catch(ArrayIndexOutOfBoundsException ignored) {}
		}

		System.out.println("Done creating labeling");

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.addTag(LABEL1, TAG1);
		model.addTag(LABEL2, TAG2);

		LabelEditorPanel<String> labelEditorPanel = new LabelEditorBdvPanel<>(model);
		labelEditorPanel.getRenderer().setTagColor(TAG1, ARGBType.rgba(0, 255, 255, 255));
		labelEditorPanel.getRenderer().setTagColor(TAG2, ARGBType.rgba(255, 0, 255, 255));
		labelEditorPanel.updateLabelRendering();

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(labelEditorPanel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) {
		new E09_BigData().run();
	}
}
