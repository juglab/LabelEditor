package sc.fiji.labeleditor.plugin.mode.timeslice;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.boundary.IntTypeBoundary;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;

@Plugin(type = LabelEditorRenderer.class, name = "time slice borders", priority = 2)
public class TimeSliceLabelEditorBorderRenderer<L> extends TimeSliceLabelEditorRenderer<L> {

	private RandomAccessibleInterval<IntType> output;

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		int timeDim = ((TimeSliceLabelEditorModel<L>)model).getTimeDimension();
		this.output = new IntTypeBoundary<>(model.labeling().getIndexImg(), timeDim);
	}

	@Override
	public void updateOnTagChange(LabelEditorModel<L> model) {
		TimeSliceLabelEditorModel<L> timeModel = (TimeSliceLabelEditorModel<L>) model;
		IntervalView<? extends IntegerType<?> > intervalView = timeModel.getIndexImgAtTime(timePoint);
		updateLUT(model, intervalView, LabelEditorTargetComponent.BORDER);
	}

	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType());
	}
}
