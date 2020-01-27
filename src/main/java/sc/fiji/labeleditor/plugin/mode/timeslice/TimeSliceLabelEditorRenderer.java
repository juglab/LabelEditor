package sc.fiji.labeleditor.plugin.mode.timeslice;

import bdv.viewer.TimePointListener;
import net.imglib2.Cursor;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import sc.fiji.labeleditor.plugin.renderers.AbstractLabelEditorRenderer;

import java.util.Arrays;
import java.util.Set;

@Plugin(type = LabelEditorRenderer.class, name = "time slice labels", priority = 1)
public class TimeSliceLabelEditorRenderer<L> extends AbstractLabelEditorRenderer<L> implements TimePointListener {

	protected int timePoint = 0;

	@Override
	public <M extends LabelEditorModel> boolean canDisplay(M model) {
		return TimeSliceLabelEditorModel.class.isAssignableFrom(model.getClass());
	}

	@Override
	public void updateOnTagChange(LabelEditorModel<L> model) {
		TimeSliceLabelEditorModel<L> timeModel = (TimeSliceLabelEditorModel<L>) model;
		IntervalView< ? extends IntegerType< ? > > intervalView = timeModel.getIndexImgAtTime(timePoint);
		updateLUT(model, intervalView, LabelEditorTargetComponent.FACE);
	}

	protected void updateLUT(LabelEditorModel<L> model, IntervalView<? extends IntegerType<?>> slice, Object targetComponent) {

		if(lut == null || lut.length != model.labeling().getMapping().numSets()) {
			lut = new int[model.labeling().getMapping().numSets()];
		} else {
			Arrays.fill(lut, 0);
		}

		if(model.colors() == null) return;

		boolean[] lutDone = new boolean[model.labeling().getMapping().numSets()];

		if(model.colors() != null) {

			Cursor<? extends IntegerType<?>> cursor = slice.cursor();

			while(cursor.hasNext()) {

				int val = cursor.next().getInteger();

				if(lutDone[val]) continue;
				lutDone[val] = true;

				Set<L> labels = model.labeling().getMapping().labelsAtIndex(val);

				if(labels.size() == 0) continue;

				lut[val] = getMixColor(model.colors(), targetComponent, labels);
			}
		}
	}

	@Override
	public void timePointChanged(int timePointIndex) {
		this.timePoint = timePointIndex;
	}
}
