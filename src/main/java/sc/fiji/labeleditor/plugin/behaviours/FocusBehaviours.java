package sc.fiji.labeleditor.plugin.behaviours;

import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class FocusBehaviours<L> implements LabelEditorBehaviours<L> {

	protected LabelEditorModel<L> model;
	protected LabelEditorController<L> control;
	protected int currentSegment = -1;
	private LabelingType<L> lastLabels = null;

	@Override
	public void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		this.model = model;
		this.control = control;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

		MouseMotionListener mml = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {}
			@Override
			public void mouseMoved(MouseEvent e) {
				focusFirstLabelAtPosition(e.getX(), e.getY());
			}
		};

		panel.addMouseMotionListener( mml );
	}

	protected synchronized void focusFirstLabelAtPosition(int x, int y) {
		try {
			model.tagging().pauseListeners();
			LabelingType<L> labels = control.interfaceInstance().findLabelsAtMousePosition(x, y, model);
			if(labels != null) {
				if(currentSegment == labels.getIndex().getInteger()) {
					model.tagging().resumeListeners();
					return;
				} else {
					defocus();
					focus(labels);
				}
			}
			model.tagging().resumeListeners();
		} catch(IndexOutOfBoundsException ignored){
			model.tagging().resumeListeners();
		}
	}

	protected void defocus() {
		if(lastLabels == null) return;
		lastLabels.forEach(label -> model.tagging().removeTagFromLabel(LabelEditorTag.FOCUS, label));
		lastLabels = null;
	}

	protected void focus(LabelingType<L> labels) {
		labels.forEach(label -> model.tagging().addTagToLabel(LabelEditorTag.FOCUS, label));
		lastLabels = labels;
		currentSegment = labels.getIndex().getInteger();
	}

}
