package com.indago.labeleditor.howto;


import com.indago.labeleditor.plugin.bdv.LabelEditorBdvPanel;
import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

/**
 * How to open an {@link ImgLabeling} in a {@link LabelEditorBdvPanel}.
 */
public class E02_Open2DLabelingConflicts {

	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel<String> panel;

	@Test
	public void run() {

		String LABEL1 = "label1";
		String LABEL2 = "label2";
		String LABEL3 = "label3";
		String LABEL4 = "label4";

		String TAG1 = "tag1";
		String TAG2 = "tag2";

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 500, 500, 2 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		//t1
		Views.interval( labels, Intervals.createMinSize( 220, 220, 0, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 220, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL3 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 220, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL4 ) );

		//t2
		Views.interval( labels, Intervals.createMinSize( 320, 320, 1, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 300, 300, 1, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL1 ) );

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		model.tagging().addTag(TAG1, LABEL1);
		model.tagging().addTag(TAG2, LABEL2);
		model.tagging().addTag(TAG1, LABEL3);
		model.tagging().addTag(TAG2, LABEL4);

		panel = new LabelEditorBdvPanel<>();
		panel.init(model);
		panel.view().colors().put(TAG1, ARGBType.rgba(0,255,255,100));
		panel.view().colors().put(TAG2, ARGBType.rgba(255,0,255,100));
		panel.control().triggerTagChange();
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

	public static void main(String... args) {
		new E02_Open2DLabelingConflicts().run();
	}

}
