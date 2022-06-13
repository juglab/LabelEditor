package sc.fiji.labeleditor.core.view;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

public interface LabelEditorOverlayRenderer<L> extends LabelEditorRenderer<L> {
	void init(LabelEditorModel<L> model, RandomAccessibleInterval<? extends ARGBType> screenImage);
}
