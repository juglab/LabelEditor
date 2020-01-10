package sc.fiji.labeleditor.core.controller;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public interface LabelEditorBehaviours<L> extends SciJavaPlugin {
	void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> controller);

	void install(Behaviours behaviours, Component panel);
}
