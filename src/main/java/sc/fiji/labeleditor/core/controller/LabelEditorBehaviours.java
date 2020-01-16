package sc.fiji.labeleditor.core.controller;

import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;

public interface LabelEditorBehaviours<L> extends SciJavaPlugin {

	void init(InteractiveLabeling<L> labeling);

	void install(Behaviours behaviours, Component panel);
}
