package sc.fiji.labeleditor.plugin.mode.timeslice;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.boundary.IntTypeBoundary;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import org.scijava.plugin.Plugin;

@Plugin(type = LabelEditorRenderer.class, name = "time slice borders", priority = 2)
public class TimeSliceLabelEditorBorderRenderer<L> extends TimeSliceLabelEditorRenderer<L> {

	private RandomAccessibleInterval output;

	@Override
	public void init(LabelEditorModel model) {
		super.init(model);
		int timeDim = ((TimeSliceLabelEditorModel)model).getTimeDimension();
		this.output = new IntTypeBoundary(model.labeling().getIndexImg(), timeDim);
	}

	@Override
	public void updateOnTagChange(LabelEditorModel model) {
		TimeSliceLabelEditorModel timeModel = (TimeSliceLabelEditorModel) model;
		IntervalView intervalView = timeModel.getIndexImgAtTime(timePoint);
		updateLUT(model, intervalView, LabelEditorTargetComponent.BORDER);
	}

	@Override
	public RandomAccessibleInterval<ARGBType> getOutput() {
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		return Converters.convert(output, converter, new ARGBType() );
	}
}
