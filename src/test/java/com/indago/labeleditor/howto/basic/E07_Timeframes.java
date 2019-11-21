package com.indago.labeleditor.howto.basic;


import com.indago.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

/**
 * How to open a labeling with separate time frame labels in the LabelEditor
 */
public class E07_Timeframes {

	/**
	 * If a labeling has separate labels per time point or if the labeling is just too large to interact with all
	 * time frames at once, you can use a {@link TimeSliceLabelEditorModel} which takes care of only handling the
	 * currently displayed time frame.
	 */
	public void run() {

		ImageJ ij = new ImageJ();
		ij.launch();

		String LABEL1 = "label1";
		String LABEL2 = "label2";
		String LABEL3 = "label3";
		String LABEL4 = "label4";

		String TAG1 = "tag1";
		String TAG2 = "tag2";

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 500, 500, 2 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		//draw labels to time point 0
		Views.interval( labels, Intervals.createMinSize( 220, 220, 0, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 220, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL3 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 220, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL4 ) );

		//draw labels to time point 1
		Views.interval( labels, Intervals.createMinSize( 320, 320, 1, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 300, 300, 1, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL1 ) );

		// create model which can handle time sliced datasets
		TimeSliceLabelEditorModel<String> model = new TimeSliceLabelEditorModel<>(labels, 2);

		model.tagging().addTagToLabel(TAG1, LABEL1);
		model.tagging().addTagToLabel(TAG2, LABEL2);
		model.tagging().addTagToLabel(TAG1, LABEL3);
		model.tagging().addTagToLabel(TAG2, LABEL4);

		model.colors().getFaceColor(TAG1).set(0,255,255,100);
		model.colors().getFaceColor(TAG2).set(255,0,255,100);

		ij.ui().show(model);
	}

	public static void main(String... args) {
		new E07_Timeframes().run();
	}

}
