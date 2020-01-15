package sc.fiji.labeleditor.core;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

public interface InteractiveLabeling<L> {
	LabelEditorModel<L> model();
	LabelEditorView<L> view();
	LabelEditorController<L> control();
}
