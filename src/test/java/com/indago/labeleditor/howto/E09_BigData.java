package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
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

	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel panel;

	@Test
	@Ignore
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
		model.tagging().addTag(LABEL1, TAG1);
		model.tagging().addTag(LABEL2, TAG2);

		panel = new LabelEditorBdvPanel<>();
		panel.init(model);
		panel.getRenderer().setTagColor(TAG1, ARGBType.rgba(0, 255, 255, 255));
		panel.getRenderer().setTagColor(TAG2, ARGBType.rgba(255, 0, 255, 255));
		panel.updateLabelRendering();

		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	@AfterClass
	public static void dispose() {
		frame.dispose();
		panel.dispose();
	}

	public static void main(String...args) {
		new E09_BigData().run();
	}
}
