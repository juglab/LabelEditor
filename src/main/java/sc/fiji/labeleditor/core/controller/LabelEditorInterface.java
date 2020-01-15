package sc.fiji.labeleditor.core.controller;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Disposable;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;

import java.awt.*;
import java.util.List;

public interface LabelEditorInterface<L> extends Disposable {
	LabelingType<L> getLabelsAtMousePosition();
	//TODO 3d position?
	LabelingType<L> findLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model);
	void set3DViewMode(boolean mode3D);
	void installBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView<L> view);
	void onViewChange(ViewChangedEvent viewChangedEvent);

	Behaviours behaviours();

	Component getComponent();

	void onTagChange(List<TagChangedEvent> tagChangedEvents);

}
