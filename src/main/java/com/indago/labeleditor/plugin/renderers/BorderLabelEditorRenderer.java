package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.boundary.IntTypeBoundary;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;

@Plugin(type = LabelEditorRenderer.class, name = "borders", priority = 1)
public class BorderLabelEditorRenderer<L> extends DefaultLabelEditorRenderer<L> {

	private RandomAccessibleInterval output;

	@Override
	public void init(LabelEditorModel model) {
		super.init(model);
		int timeDim = model.options().getTimeDimension();
		this.output = new IntTypeBoundary(model.labels().getIndexImg(), timeDim);
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

}
