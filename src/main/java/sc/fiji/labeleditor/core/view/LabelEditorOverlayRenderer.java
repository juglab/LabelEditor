package sc.fiji.labeleditor.core.view;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

public interface LabelEditorOverlayRenderer<L> extends LabelEditorRenderer<L> {
	void updateScreenImage(RandomAccessibleInterval<IntType> screenImage);
	void init(LabelEditorModel<L> model, RandomAccessibleInterval<IntType> screenImage);
}
