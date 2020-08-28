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
package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Point;
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
import net.imglib2.roi.labeling.LabelRegionCursor;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.NativeType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.application.InteractiveWatershedCommand;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class SplitLabels<L> implements Behaviour {

	@Parameter
	private OpService opService;
	@Parameter
	private CommandService commandService;

	private final InteractiveLabeling<L> labeling;

	public SplitLabels(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void splitSelected() {
		if(opService == null) {
			throw new RuntimeException("No OpService available. You have to inject your LabelEditorPanel with a context to use this behaviour.");
		}
		List<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		selected.forEach(label -> {
			try {
				splitInteractively(label);
			} catch (ExecutionException | InterruptedException e) {
				e.printStackTrace();
			}
		});
		labeling.model().notifyLabelingListeners();
	}

	public <T extends NativeType<T>> void splitInteractively(L label) throws ExecutionException, InterruptedException {
		LabelRegions<L> regions = new LabelRegions<>(labeling.model().labeling());
		LabelRegion<L> region = regions.getLabelRegion(label);
		ImgLabeling<L, IntType> cropLabeling = createCroppedLabeling(label, region);
		RandomAccessibleInterval data = createCroppedData(region);
		CommandModule out = commandService.run(
				InteractiveWatershedCommand.class, true,
				"labeling", cropLabeling,
				"data", data).get();
		LabelEditorModel outModel = (LabelEditorModel) out.getOutput("output");
		if(outModel != null) {
			System.out.println("new labels: " + outModel.labeling().getMapping().getLabels().size());
//			TODO add new labels to model labeling
//			Set<Object> tags = model.tagging().getTags(label);
//			newLabeling.forEach(newlabel -> tags.forEach(tag -> model.tagging().addTag(tag, newlabel)));
			labeling.model().notifyLabelingListeners();
		}


//		ImgLabeling< Integer, IntType > watershed = new ImgLabeling<>( backing );
//
//		IntervalView dataCrop = Views.zeroMin(Views.interval(data, region));
//		RandomAccessibleInterval gaussCrop = opService.filter().gauss(dataCrop, sigma);
//		final ImgLabeling<Integer, IntType> seeds = findAndDisplayLocalMaxima(gaussCrop);
//		Img invertedDataCrop = opService.create().img(Views.iterable(dataCrop));
//		Set<L> newlabels = split(label, model.labels(), model.getData(), 1, opService);
	}

	private RandomAccessibleInterval createCroppedData(LabelRegion<L> region) {
		return opService.copy().rai(Views.zeroMin(Views.interval(labeling.model().getData(), region)));
	}

	private ImgLabeling<L, IntType> createCroppedLabeling(L label, LabelRegion<L> region) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create( Views.zeroMin(region) );
		ImgLabeling<L, IntType> cropLabeling = new ImgLabeling<>(backing);
		Point offset = new Point(region.numDimensions());
		for (int i = 0; i < region.numDimensions(); i++) {
			offset.setPosition(-region.min(i), i);
		}
		LabelRegionCursor inCursor = region.localizingCursor();
		RandomAccess<LabelingType<L>> outRA = cropLabeling.randomAccess();
		while(inCursor.hasNext()) {
			inCursor.next();
			outRA.setPosition(inCursor);
			outRA.move(offset);
			outRA.get().add(label);
		}
		return cropLabeling;
	}

	public static <L> Set<L> split(L label, ImgLabeling<L, ? extends IntegerType<?> > labeling, RandomAccessibleInterval data, double sigma, OpService opService) {
		LabelRegions regions = new LabelRegions<>(labeling);
		LabelRegion<Integer> region = regions.getLabelRegion(label);
		Img<BitType> mask = createMask(opService, region);
		ImgLabeling<Integer, IntType> watershed = createWatershedResultLabeling(region);
		Img watershedInput = createWatershedInput(data, sigma, opService, region);
		final ImgLabeling<Integer, IntType> seeds = findAndDisplayLocalMinima(watershedInput, mask);
		opService.image().watershed(watershed, watershedInput, seeds, true, false, mask);

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

	private static Img<BitType> createMask(OpService opService, LabelRegion<Integer> region) {
		Img<BitType> mask = opService.create().img(Views.zeroMin(region), new BitType());
		Point offset = new Point(region.numDimensions());
		for (int i = 0; i < region.numDimensions(); i++) {
			offset.setPosition(-region.min(i), i);
		}
		LabelRegionCursor regionCursor = region.localizingCursor();
		RandomAccess<BitType> maskRA = mask.randomAccess();
		while(regionCursor.hasNext()) {
			regionCursor.next();
			maskRA.setPosition(regionCursor);
			maskRA.move(offset);
			maskRA.get().setOne();
		}
		return mask;
	}

	private static Img createWatershedInput(RandomAccessibleInterval data, double sigma, OpService opService, LabelRegion<Integer> region) {
		IntervalView dataCrop = Views.zeroMin(Views.interval(data, region));
		RandomAccessibleInterval gaussCrop = opService.filter().gauss(dataCrop, sigma);
		Img invertedDataCrop = opService.create().img(Views.iterable(dataCrop));
		opService.image().invert(invertedDataCrop, Views.iterable(gaussCrop));
		return invertedDataCrop;
	}

	private static ImgLabeling<Integer, IntType> createWatershedResultLabeling(LabelRegion<Integer> region) {
		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( region );
		return new ImgLabeling<>( backing );
	}


	public static < T extends Comparable< T >> ImgLabeling<Integer, IntType>
	findAndDisplayLocalMinima(RandomAccessibleInterval<T> source, Img<BitType> mask) {
		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( source );
		ImgLabeling< Integer, IntType > res = new ImgLabeling<>( backing );
		RandomAccess<LabelingType<Integer>> ra = res.randomAccess();
		int count = 0;
		Interval interval = Intervals.expand( source, -1 );
		source = Views.interval( source, interval );
		final Cursor< T > center = Views.iterable( source ).cursor();
		RandomAccess<BitType> maskRA = mask.randomAccess();
		final RectangleShape shape = new RectangleShape( 1, true );
		for ( final Neighborhood< T > localNeighborhood : shape.neighborhoods( source ) ) {
			final T centerValue = center.next();
			maskRA.setPosition(center);
			if(maskRA.get().get()) {
				boolean isMinimum = true;
				for ( final T value : localNeighborhood ) {
					if ( centerValue.compareTo( value ) >= 0 ) {
						isMinimum = false;
						break;
					}
				}
				if ( isMinimum ) {
					ra.setPosition(center);
					ra.get().add(count++);
				}
			}
		}
		return res;
	}

}
