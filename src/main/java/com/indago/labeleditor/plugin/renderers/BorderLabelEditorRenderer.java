package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.view.LabelEditorTagColors;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;

import java.util.Map;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "borders")
public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {

	private RandomAccessibleInterval<IntType> output;

	public BorderLabelEditorRenderer() {}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		super.init(labels);
		this.output = new DiskCachedCellImgFactory<>(new IntType()).create(labels);
		updateOutput();
	}

	@Override
	public void updateOnLabelingChange() {
		if(labels != null && output != null) updateOutput();
	}

	@Override
	public synchronized void updateOnTagChange(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, LabelEditorTagColors tagColors) {

		int[] lut;

		// our LUT has one entry per index in the index img of our labeling
		lut = new int[mapping.numSets()];

		for (int i = 0; i < lut.length; i++) {
			// get all labels of this index
			Set<L> labels = mapping.labelsAtIndex(i);

			// if there are no labels, we don't need to check for tags and can continue
			if(labels.size() == 0) continue;

			// get all tags associated with the labels of this index
			Set<Object> mytags = filterTagsByLabels( tags, labels);

			lut[i] = mixColors(mytags, tagColors, LabelEditorTargetComponent.BORDER);
//			lut[i] = ARGBType.rgba(255,255,255,Math.min(labels.size()*50, 255));

		}

		this.lut = lut;
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType() );
	}

	private void updateOutput() {
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
