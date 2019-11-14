package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.LabelEditorOptions;
import com.indago.labeleditor.core.model.LabelEditorModel;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public interface LabelEditorBehaviours<L> extends SciJavaPlugin {
	void init(LabelEditorModel<L> model, LabelEditorController<L> controller);

	void install(Behaviours behaviours, Component panel);
}
