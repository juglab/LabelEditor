package sc.fiji.labeleditor.core.controller;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.util.Set;

public interface InteractiveLabeling<L> {
	LabelEditorModel<L> model();
	LabelEditorView<L> view();
	LabelEditorInterface interfaceInstance();
	RandomAccessibleInterval<LabelingType<L>> getLabelingInScope();
	Set<L> getLabelSetInScope();
}
