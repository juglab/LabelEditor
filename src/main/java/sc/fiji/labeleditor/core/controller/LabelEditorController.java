package sc.fiji.labeleditor.core.controller;

import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;

import java.util.Set;

public interface LabelEditorController<L> {

	InteractiveLabeling init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance);

	void addDefaultBehaviours();

	void triggerLabelingChange();

	LabelEditorInterface<L> interfaceInstance();

	void install(LabelEditorBehaviours behaviour);

	IterableInterval<LabelingType<L>> labelingInScope();

	Set<L> labelSetInScope();
}
