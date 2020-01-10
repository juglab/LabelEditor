package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
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

		TimeSliceLabelEditorModel<String> model = new TimeSliceLabelEditorModel<>(labels, backing, 2);
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
