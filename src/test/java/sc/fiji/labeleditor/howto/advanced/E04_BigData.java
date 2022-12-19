/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
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
package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;

import java.util.Random;

/**
 * How larger datasets appear in the LabelEditor
 */
public class E04_BigData {

	/**
	 * This example creates a {@link DiskCachedCellImg}, adds 100 labels randomly which should result in ~50.000 labelsets.
	 * The example also demonstrates how to use the {@link TimeSliceLabelEditorModel} to display larger data.
	 * In this mode, only the current timeframe is taken into consideration when doing operations like select all, etc.
	 */
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
			for (int j = 0; j < 420; j++) {
				int finalI = i;
				try {
				Views.interval( labels,
						Intervals.createMinSize(
								random.nextInt((int) backing.dimension(0)),
								random.nextInt((int) backing.dimension(1)),
								random.nextInt((int) backing.dimension(2)),
								100, 100, 1 ) ).forEach(pixel -> pixel.add( "label"+ finalI) );
				} catch(ArrayIndexOutOfBoundsException ignored) {}
			}
			System.out.println("done with label " + i);
		}

		System.out.println("Done creating labeling");

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels, backing);
		model.tagging().addTagToLabel(LABEL1, TAG1);
		model.tagging().addTagToLabel(LABEL2, TAG2);

		model.colors().getFaceColor(TAG1).set(0, 255, 255);
		model.colors().getFaceColor(TAG2).set(255, 0, 255);

		ij.ui().show(model);
	}

	public static void main(String...args) {
		new E04_BigData().run();
	}
}
