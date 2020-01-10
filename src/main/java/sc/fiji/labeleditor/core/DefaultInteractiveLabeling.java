package sc.fiji.labeleditor.core;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

public class DefaultInteractiveLabeling implements InteractiveLabeling {
	private LabelEditorModel model;
	private LabelEditorView view;
	private LabelEditorController control;

	public DefaultInteractiveLabeling(LabelEditorModel model, LabelEditorView view, LabelEditorController control) {
		this.model = model;
		this.view = view;
		this.control = control;
	}

	@Override
	public LabelEditorModel model() {
		return model;
	}

	@Override
	public LabelEditorView view() {
		return view;
	}

	@Override
	public LabelEditorController control() {
		return control;
	}

	@Override
	public String toString() {
		return model().getName();
	}
}
