package com.indago.labeleditor.plugin.behaviours.modification;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BoolType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.Behaviour;

import java.util.HashSet;
import java.util.Set;

public class SplitSelectedLabels<L> implements Behaviour {

	private final LabelEditorController controller;
	private final LabelEditorModel<L> model;
	@Parameter
	private OpService opService;

	public SplitSelectedLabels(LabelEditorModel model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void splitSelected() {
		if(opService == null) {
			throw new RuntimeException("No OpService available. You have to inject your LabelEditorPanel with a context to use this behaviour.");
		}
		Set<L> selected = model.tagging().getLabels(LabelEditorTag.SELECTED);
		selected.forEach(label -> {
			Set<Object> tags = model.tagging().getTags(label);
			Set<L> newlabels = split(label, model.labels(), model.getData(), opService);
			newlabels.forEach(newlabel -> tags.forEach(tag -> model.tagging().addTag(tag, newlabel)));
		});
		controller.triggerLabelingChange();
	}

	static <L> Set<L> split(L label, ImgLabeling<L, IntType> labeling, RandomAccessibleInterval data, OpService opService) {
		LabelRegions regions = new LabelRegions<>(labeling);
		LabelRegion<Integer> region = regions.getLabelRegion(label);

		System.out.println(region.getCenterOfMass());

		IntervalView<BoolType> zeroRegion = Views.zeroMin(region);

		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( region );
		ImgLabeling< Integer, IntType > watershed = new ImgLabeling<>( backing );

		IntervalView dataCrop = Views.zeroMin(Views.interval(data, region));
		RandomAccessibleInterval gaussCrop = opService.filter().gauss(dataCrop, 1);
		final ImgLabeling<Integer, IntType> seeds = findAndDisplayLocalMaxima(gaussCrop);
		Img invertedDataCrop = opService.create().img(Views.iterable(dataCrop));
		opService.image().invert(invertedDataCrop, Views.iterable(gaussCrop));
		opService.image().watershed(watershed, invertedDataCrop, seeds, false, false, zeroRegion);

		//writing the watershedded values back into the original labeling, with the integer offset to not have duplicate labels
		IntervalView<LabelingType<L>> labelingCrop = Views.zeroMin(Views.interval(labeling, region));
		Cursor<LabelingType<Integer>> waterShedCursor = watershed.localizingCursor();
		RandomAccess<LabelingType<L>> labelingRa = labelingCrop.randomAccess();
		int startVal = labeling.getMapping().getLabels().size();
		Set<L> res = new HashSet<>();
		while(waterShedCursor.hasNext()) {
			LabelingType<Integer> vals = waterShedCursor.next();
			labelingRa.setPosition(waterShedCursor);
			vals.forEach(val -> {
				L newlabel = (L) new Integer(val + startVal);
				res.add(newlabel);
				labelingRa.get().add(newlabel);
			});
		}
		DeleteLabels.delete(label, labeling);
		return res;
	}


	public static < T extends Comparable< T >> ImgLabeling<Integer, IntType>
	findAndDisplayLocalMaxima(RandomAccessibleInterval< T > source ) {
		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( source );
		ImgLabeling< Integer, IntType > res = new ImgLabeling<>( backing );
		RandomAccess<LabelingType<Integer>> ra = res.randomAccess();
		int count = 0;
		Interval interval = Intervals.expand( source, -1 );
		source = Views.interval( source, interval );
		final Cursor< T > center = Views.iterable( source ).cursor();
		final RectangleShape shape = new RectangleShape( 1, true );
		for ( final Neighborhood< T > localNeighborhood : shape.neighborhoods( source ) ) {
			final T centerValue = center.next();
			boolean isMaximum = true;
			for ( final T value : localNeighborhood ) {
				if ( centerValue.compareTo( value ) <= 0 ) {
					isMaximum = false;
					break;
				}
			}
			if ( isMaximum ) {
				ra.setPosition(center);
				ra.get().add(count++);
			}
		}
		return res;
	}

}