package sc.fiji.labeleditor.core;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

public class DefaultInteractiveLabeling<L> implements InteractiveLabeling<L> {
	private LabelEditorModel<L> model;
	private LabelEditorView<L> view;
	private LabelEditorController<L> control;

	public DefaultInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		this.model = model;
		this.view = view;
		this.control = control;
	}

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	@Override
	public LabelEditorView<L> view() {
		return view;
	}

	@Override
	public LabelEditorController<L> control() {
		return control;
	}

	@Override
	public String toString() {
		return model().getName();
	}
}
