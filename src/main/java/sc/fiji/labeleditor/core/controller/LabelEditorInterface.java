package sc.fiji.labeleditor.core.controller;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;

import java.awt.*;
import java.util.List;

public interface LabelEditorInterface<L> {
	//TODO 3d position?
	LabelingType<L> findLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model);
	void onViewChange(ViewChangedEvent viewChangedEvent);

	Behaviours behaviours();

	void install(LabelEditorBehaviours behaviour, InteractiveLabeling labeling);

	Component getComponent();

	void onTagChange(List<TagChangedEvent> tagChangedEvents);

	void display(LabelEditorView<L> view);

	void installBehaviours(InteractiveLabeling<L> labeling);
}
