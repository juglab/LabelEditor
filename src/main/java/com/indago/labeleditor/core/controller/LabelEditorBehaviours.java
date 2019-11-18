package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public interface LabelEditorBehaviours<L> extends SciJavaPlugin {
	void init(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView<L> view);

	void install(Behaviours behaviours, Component panel);
}
