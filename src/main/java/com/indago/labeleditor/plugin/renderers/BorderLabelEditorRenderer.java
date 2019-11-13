package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
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
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "borders", priority = 1)
public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {

	private RandomAccessibleInterval<IntType> output;

	public BorderLabelEditorRenderer() {}

	@Override
	public void init(LabelEditorModel model) {
		super.init(model);
		this.output = new DiskCachedCellImgFactory<>(new IntType()).create(model.labels());
		updateOutput();
	}

	@Override
	public void updateOnLabelingChange() {
		if(model.labels() != null && output != null) updateOutput();
	}

	@Override
	public synchronized void updateOnTagChange(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, LabelEditorTagColors tagColors) {
		updateLUT(mapping, tagColors, LabelEditorTargetComponent.BORDER);
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType() );
	}

	private void updateOutput() {
		RandomAccess<IntType> randomAccess = model.labels().getIndexImg().randomAccess();
		int nothing = ARGBType.rgba(0, 0, 0, 0);
		DiamondShape shape = new DiamondShape(1);

		RandomAccessible< Neighborhood<IntType>> neighborhoodsAccessible = shape.neighborhoodsRandomAccessible(Views.extendMirrorSingle(model.labels().getIndexImg()));

		IntervalView<Neighborhood<IntType>> neighborhoods = Views.interval(neighborhoodsAccessible, model.labels());

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
