package sc.fiji.labeleditor.core;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

public interface InteractiveLabeling {
	LabelEditorModel model();
	LabelEditorView view();
	LabelEditorController control();
}
