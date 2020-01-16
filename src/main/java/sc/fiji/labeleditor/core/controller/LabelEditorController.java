package sc.fiji.labeleditor.core.controller;

import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.util.Set;

public interface LabelEditorController<L> {

	InteractiveLabeling<L> init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance);

	LabelEditorInterface<L> interfaceInstance();

	IterableInterval<LabelingType<L>> getLabelingInScope();

	Set<L> getLabelSetInScope();
}
