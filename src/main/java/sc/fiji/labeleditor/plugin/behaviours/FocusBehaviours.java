package sc.fiji.labeleditor.plugin.behaviours;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class FocusBehaviours<L> implements LabelEditorBehaviours<L> {

	protected int currentSegment = -1;
	private LabelingType<L> lastLabels = null;
	protected InteractiveLabeling<L> labeling;

	@Override
	public void init(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
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
			labeling.model().tagging().pauseListeners();
			LabelingType<L> labels = labeling.interfaceInstance().findLabelsAtMousePosition(x, y, labeling);
			if(labels != null) {
				if(currentSegment == labels.getIndex().getInteger()) {
					labeling.model().tagging().resumeListeners();
					return;
				} else {
					defocus();
					focus(labels);
				}
			}
			labeling.model().tagging().resumeListeners();
		} catch(IndexOutOfBoundsException ignored){
			labeling.model().tagging().resumeListeners();
		}
	}

	protected void defocus() {
		if(lastLabels == null) return;
		lastLabels.forEach(label -> labeling.model().tagging().removeTagFromLabel(LabelEditorTag.MOUSE_OVER, label));
		lastLabels = null;
	}

	protected void focus(LabelingType<L> labels) {
		labels.forEach(label -> labeling.model().tagging().addTagToLabel(LabelEditorTag.MOUSE_OVER, label));
		lastLabels = labels;
		currentSegment = labels.getIndex().getInteger();
	}

}
