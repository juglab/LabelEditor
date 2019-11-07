package com.indago.labeleditor.display;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.convolution.kernel.Kernel1D;
import net.imglib2.algorithm.convolution.kernel.SeparableKernelConvolution;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {


	private ImgLabeling<L, IntType> labels;
	private RandomAccessibleInterval<IntType> output;

	public BorderLabelEditorRenderer() {}

	@Override
	public String getName() {
		return "borders";
	}

	@Override
	public synchronized void update(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, Map<Object, LUTChannel> tagColors) {

		if(labels != null && output != null) updateOutput(labels, output);

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[mapping.numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<L> labels = mapping.labelsAtIndex(i);

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			lut[i] = ARGBType.rgba(255,255,255,Math.min(labels.size()*50, 255));

		}

		this.lut = lut;
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getRenderedLabels(ImgLabeling<L, IntType> labels) {

		RandomAccessibleInterval<IntType> output = new ArrayImgFactory<>(new IntType()).create(labels);
		this.labels = labels;
		this.output = output;

		updateOutput(labels, output);
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
//		ImageJFunctions.show(output, "output");
		return Converters.convert(output, converter, new ARGBType() );
	}

	private void updateOutput(ImgLabeling<L, IntType> labels, RandomAccessibleInterval<IntType> output) {
		RandomAccess<IntType> randomAccess = labels.getIndexImg().randomAccess();
		int nothing = ARGBType.rgba(0, 0, 0, 0);
		DiamondShape shape = new DiamondShape(1);

		RandomAccessible< Neighborhood<IntType>> neighborhoodsAccessible = shape.neighborhoodsRandomAccessible(Views.extendMirrorSingle(labels.getIndexImg()));

		IntervalView<Neighborhood<IntType>> neighborhoods = Views.interval(neighborhoodsAccessible, labels);

		Cursor<Neighborhood<IntType>> neighborhoodCursor = neighborhoods.localizingCursor();
		RandomAccess<IntType> outputRa = output.randomAccess();
		while(neighborhoodCursor.hasNext()) {
			Neighborhood<IntType> neighborhood = neighborhoodCursor.next();
			float[] pos = new float[neighborhoodCursor.numDimensions()];
			neighborhoodCursor.localize(pos);
			outputRa.setPosition(neighborhoodCursor);
			randomAccess.setPosition(neighborhoodCursor);
			IntType centerPixel = randomAccess.get();
			IntType o = outputRa.get();
			if(centerPixel.get() == 0) { o.set(nothing); continue;}
//			if(pos[0] == labels.dimension(0)-1) {
//				System.out.print("pos: " + Arrays.toString(pos) + " neighborhood: ");
//				neighborhood.forEach(neighbor -> {
//					try {
//						System.out.print(neighbor.get() + " ");
//					} catch(ArrayIndexOutOfBoundsException ignored) {System.out.print("X ");}
//				});
//				System.out.println();
//			}
			boolean found = false;
			for (IntType neighbor : neighborhood) {
				if (!neighbor.equals(centerPixel)) {
					o.set(centerPixel);
					found = true;
					break;
				}
			}
			if(!found) o.set(nothing);
		}
	}


}
