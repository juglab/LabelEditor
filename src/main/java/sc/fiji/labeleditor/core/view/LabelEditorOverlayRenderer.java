package sc.fiji.labeleditor.core.view;

import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

public interface LabelEditorOverlayRenderer<L> extends LabelEditorRenderer<L> {
	void init(LabelEditorModel<L> model, RandomAccessibleInterval<? extends ARGBType> screenImage);
}
