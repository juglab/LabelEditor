package sc.fiji.labeleditor.plugin.mode.timeslice;

import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labeleditor.core.model.AbstractLabelEditorModel;
import sc.fiji.labeleditor.core.model.DefaultColors;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class TimeSliceLabelEditorModel<L> extends AbstractLabelEditorModel<L> {

	private int timeDimension = -1;

	public TimeSliceLabelEditorModel(ImgLabeling<L, IntType> labeling, int timeDimension) {
		super(labeling);
		this.timeDimension = timeDimension;
		addDefaultColorsets();
	}

	public TimeSliceLabelEditorModel(ImgLabeling<L, IntType> labeling, RandomAccessibleInterval data, int timeDimension) {
		super(labeling, data);
		this.timeDimension = timeDimension;
		addDefaultColorsets();
	}

	protected void addDefaultColorsets() {
		colors().getDefaultFaceColor().set(DefaultColors.defaultFace());
		colors().getDefaultBorderColor().set(DefaultColors.defaultBorder());
		colors().getSelectedFaceColor().set(DefaultColors.selectedFace());
		colors().getSelectedBorderColor().set(DefaultColors.selectedBorder());
		colors().getFocusFaceColor().set(DefaultColors.focusFace());
		colors().getFocusBorderColor().set(DefaultColors.focusBorder());
	}

	public int getTimeDimension() {
		return timeDimension;
	}

	public IntervalView<IntType> getIndexImgAtTime(long currentTimePoint) {
		return Views.hyperSlice(labeling().getIndexImg(), getTimeDimension(), currentTimePoint);
	}

	public IntervalView getLabelingAtTime(long currentTimePoint) {
		return Views.hyperSlice(labeling(), getTimeDimension(), currentTimePoint);
	}
}
