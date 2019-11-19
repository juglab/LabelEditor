package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class E09_BigData {

	public void run() {

		ImageJ ij = new ImageJ();
		ij.launch();

		DiskCachedCellImg<IntType, ?> backing = new DiskCachedCellImgFactory<>(new IntType()).create( 1000, 1000, 500 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		String LABEL1 = "label1";
		String LABEL2 = "label2";

		String TAG1 = "tag1";
		String TAG2 = "tag2";

		Random random = new Random();

		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < 100; j++) {
				int finalI = i;
				try {
				Views.interval( labels,
						Intervals.createMinSize(
								random.nextInt((int) backing.dimension(0)),
								random.nextInt((int) backing.dimension(1)),
								random.nextInt((int) backing.dimension(2)),
								20, 20, 20 ) ).forEach(pixel -> pixel.add( "label"+ finalI) );
				} catch(ArrayIndexOutOfBoundsException ignored) {}
			}
			System.out.println("done with label " + i);
		}

		System.out.println("Done creating labeling");

		DefaultLabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.init(labels);
		model.tagging().addTag(LABEL1, TAG1);
		model.tagging().addTag(LABEL2, TAG2);

		model.colors().setFaceColor(TAG1,0, 255, 255, 255);
		model.colors().setFaceColor(TAG2,255, 0, 255, 255);

		model.setData(backing);

		LabelEditorPanel panel = new LabelEditorBdvPanel<>();
		ij.context().inject(panel);
		panel.init(model);

		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(1000,1000));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) {
		new E09_BigData().run();
	}
}
