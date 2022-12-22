/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.howto.basic;


import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;
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
