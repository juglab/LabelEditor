package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.model.colors.LabelEditorTagColors;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Map;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "borders", priority = 1)
public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {

	private RandomAccessibleInterval output;
	private int timePoint = 0;

	public BorderLabelEditorRenderer() {}

	@Override
	public void init(LabelEditorModel model) {
		super.init(model);
		Interval outputInterval = model.labels();
		int timeDim = model.options().getTimeDimension();
		if(timeDim >= 0) {
			outputInterval = Views.hyperSlice(model.labels(), timeDim, 0);
		}
		this.output = new DiskCachedCellImgFactory<>(new IntType()).create(outputInterval);
		updateOutput();
	}

	@Override
	public void updateOnLabelingChange() {
		if(model.labels() != null && output != null) updateOutput();
	}

	@Override
	public synchronized void updateOnTagChange(LabelEditorModel model) {
		updateLUT(model.labels().getMapping(), model.colors(), LabelEditorTargetComponent.BORDER);
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType() );
	}

	private void updateOutput() {
		RandomAccessibleInterval currentImg = model.labels().getIndexImg();
		if(model.options().getTimeDimension() >= 0) {
			currentImg = Views.hyperSlice(model.labels().getIndexImg(), model.options().getTimeDimension(), timePoint);
		}
		RandomAccess<IntType> randomAccess = currentImg.randomAccess();
		int nothing = ARGBType.rgba(0, 0, 0, 0);
		DiamondShape shape = new DiamondShape(1);

		RandomAccessible< Neighborhood<IntType>> neighborhoodsAccessible = shape.neighborhoodsRandomAccessible(Views.extendMirrorSingle(currentImg));

		IntervalView<Neighborhood<IntType>> neighborhoods = Views.interval(neighborhoodsAccessible, currentImg);

		Cursor<Neighborhood<IntType>> neighborhoodCursor = neighborhoods.localizingCursor();
		RandomAccess<IntType> outputRa = output.randomAccess();

		while(neighborhoodCursor.hasNext()) {
			Neighborhood<IntType> neighborhood = neighborhoodCursor.next();
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

	@Override
	public void updateTimePoint(int timePointIndex) {
		this.timePoint = timePointIndex;
		updateOutput();
	}
}
