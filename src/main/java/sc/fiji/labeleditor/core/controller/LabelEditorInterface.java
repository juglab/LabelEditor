package sc.fiji.labeleditor.core.controller;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;

import java.awt.*;
import java.util.List;

public interface LabelEditorInterface {
	//TODO 3d position?
	<L> LabelingType<L> findLabelsAtMousePosition(int x, int y, InteractiveLabeling<L> labeling);
	void onViewChange(ViewChangedEvent viewChangedEvent);

	<L> void install(LabelEditorBehaviours<L> behaviour, InteractiveLabeling<L> labeling);

	Component getComponent();

	void onTagChange(List<TagChangedEvent> tagChangedEvents);

	<L> void display(LabelEditorView<L> view);

	<L> void installBehaviours(InteractiveLabeling<L> labeling);

	<L> Behaviours behaviours(InteractiveLabeling<L> interactiveLabeling);

	void setRendererActive(LabelEditorRenderer renderer, boolean active);
}
