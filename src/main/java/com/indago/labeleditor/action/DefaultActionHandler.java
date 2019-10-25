package com.indago.labeleditor.action;

import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DefaultActionHandler <U> implements ActionHandler {

	private final LabelEditorPanel<?, U> panel;
	private LabelingType<U> currentLabels;
	int currentSegment;

	public DefaultActionHandler(LabelEditorPanel<?,U> panel) {
		this.panel = panel;
	}

	@Override
	public void init() {
		initMouseMotionListener();
		installBdvBehaviours();
	}

	private void initMouseMotionListener() {
		MouseMotionListener mml = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {}
			@Override
			public void mouseMoved(MouseEvent e) {
				handleMouseMove();
			}
		};

		panel.bdvGetHandlePanel().getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	private void installBdvBehaviours() {
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( panel.bdvGetHandlePanel().getBdvHandle().getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				(ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> handleWheelRotation(wheelRotation, isHorizontal),
				"browse segments",
				"shift scroll" );
		behaviours.behaviour(
				(ClickBehaviour) (arg0, arg1) -> handleClick(),
				"select current segment",
				"button1" );
	}

	private void handleMouseMove() {
		LabelingType<U> labels = getLabelsAtMousePosition();
		int intIndex;
		try {
			intIndex = labels.getIndex().getInteger();
		} catch(ArrayIndexOutOfBoundsException exc) {return;}
		if(intIndex == currentSegment) return;
		currentSegment = intIndex;
		new Thread(() -> {
			defocusAll();
			currentLabels = labels;
			labels.forEach(this::focus);
			panel.updateLUT();
		}).start();
	}

	private void handleClick() {
		if (noLabelsAtMousePosition()) {
			deselectAll();
		} else {
			selectFirst(currentLabels);
		}
		panel.updateLUT();
	}

	private boolean noLabelsAtMousePosition() {
		return currentLabels == null || currentLabels.size() == 0;
	}

	private void handleWheelRotation(double direction, boolean isHorizontal) {
		if(noLabelsAtMousePosition()) return;
		if(!anySelected(currentLabels))
			selectFirst(currentLabels);
		if ( !isHorizontal )
			if(direction > 0)
				selectNext(currentLabels);
			else
				selectPrevious(currentLabels);
	}

	private LabelingType<U> getLabelsAtMousePosition() {
		Point pos = getMousePositionInBDV();
		return getLabelsAtPosition(pos);
	}

	private Point getMousePositionInBDV() {
		RealPoint mousePointer = new RealPoint(3);
		panel.bdvGetHandlePanel().getViewerPanel().getGlobalMouseCoordinates( mousePointer );
		final int x = ( int ) mousePointer.getFloatPosition( 0 );
		final int y = ( int ) mousePointer.getFloatPosition( 1 );
		int time = panel.bdvGetHandlePanel().getViewerPanel().getState().getCurrentTimepoint();
		return new Point(x, y, time);
	}

	protected LabelingType<U> getLabelsAtPosition(Localizable pos) {
		RandomAccess<LabelingType<U>> ra = panel.getModel().getLabels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

	private void selectFirst(LabelingType<U> currentLabels) {
		List<U> orderedLabels = new ArrayList<>(currentLabels);
		orderedLabels.sort(panel.getModel()::compare);
		deselectAll();
		select(orderedLabels.get(0));
	}

	private boolean isSelected(U label) {
		return panel.getModel().getTags(label).contains(LabelEditorTag.SELECTED);
	}

	private boolean anySelected(LabelingType<U> labels) {
		return labels.stream().anyMatch(label -> panel.getModel().getTags(label).contains(LabelEditorTag.SELECTED));
	}

	private void select(U label) {
		panel.getModel().addTag(label, LabelEditorTag.SELECTED);
	}

	private void selectPrevious(LabelingType<U> labels) {
		System.out.println("select previous");
		List<U> reverseLabels = new ArrayList<>(labels);
		Collections.reverse(reverseLabels);
		selectNext(reverseLabels);
	}

	private void selectNext(Collection<U> labels) {
		System.out.println("select previous");
		boolean foundSelected = false;
		for (U label : labels) {
			if(isSelected(label)) {
				foundSelected = true;
				deselect(label);
			} else {
				if(foundSelected) {
					select(label);
					return;
				}
			}
		}
	}

	private void deselect(U label) {
		panel.getModel().removeTag(label, LabelEditorTag.SELECTED);
	}

	private void deselectAll() {
		panel.getModel().removeTag(LabelEditorTag.SELECTED);
	}

	private void defocusAll() {
		panel.getModel().removeTag(LabelEditorTag.MOUSE_OVER);
	}

	private void focus(U label) {
		panel.getModel().addTag(label, LabelEditorTag.MOUSE_OVER);
	}
}
