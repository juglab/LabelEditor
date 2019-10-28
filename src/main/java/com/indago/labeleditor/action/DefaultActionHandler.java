package com.indago.labeleditor.action;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.LabelEditorModel;
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

public class DefaultActionHandler <L> implements ActionHandler<L> {

	private final BdvHandlePanel panel;
	private final LabelEditorModel<L> model;
	private final LabelEditorRenderer renderer;
	private LabelingType<L> currentLabels;
	private int currentSegment;
	private boolean mode3D;

	public DefaultActionHandler(BdvHandlePanel bdvHandlePanel, LabelEditorModel<L> model, LabelEditorRenderer renderer) {
		this.model = model;
		this.renderer = renderer;
		this.panel = bdvHandlePanel;
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
				handleMouseMove(e);
			}
		};

		panel.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	private void installBdvBehaviours() {
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( panel.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				(ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> handleWheelRotation(wheelRotation, isHorizontal),
				"browse segments",
				"shift scroll" );
		behaviours.behaviour(
				(ClickBehaviour) (arg0, arg1) -> handleClick(),
				"select current segment",
				"button1" );
	}

	private void handleMouseMove(MouseEvent e) {
		LabelingType<L> labels = getLabelsAtMousePosition(e);
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
			updateLabelRendering();
		}).start();
	}

	private void handleClick() {
		if (noLabelsAtMousePosition()) {
			deselectAll();
		} else {
			selectFirst(currentLabels);
		}
		updateLabelRendering();
	}

	private void updateLabelRendering() {
		renderer.update();
		panel.getViewerPanel().requestRepaint();
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

	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e) {
		Point pos = getMousePositionInBDV();
		return getLabelsAtPosition(pos);
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	private Point getMousePositionInBDV() {
		RealPoint mousePointer = new RealPoint(3);
		panel.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
		final int x = ( int ) mousePointer.getFloatPosition( 0 );
		final int y = ( int ) mousePointer.getFloatPosition( 1 );
		int time = panel.getViewerPanel().getState().getCurrentTimepoint();
		if(mode3D) {
			final int z = ( int ) mousePointer.getFloatPosition( 2 );
			return new Point(x, y, z, time);
		}
		return new Point(x, y, time);
	}

	private LabelingType<L> getLabelsAtPosition(Localizable pos) {
		RandomAccess<LabelingType<L>> ra = model.getLabels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

	private void selectFirst(LabelingType<L> currentLabels) {
		List<L> orderedLabels = new ArrayList<>(currentLabels);
		orderedLabels.sort(model::compare);
		deselectAll();
		select(orderedLabels.get(0));
	}

	private boolean isSelected(L label) {
		return model.getTags(label).contains(LabelEditorTag.SELECTED);
	}

	private boolean anySelected(LabelingType<L> labels) {
		return labels.stream().anyMatch(label -> model.getTags(label).contains(LabelEditorTag.SELECTED));
	}

	private void select(L label) {
		model.addTag(LabelEditorTag.SELECTED, label);
	}

	private void selectPrevious(LabelingType<L> labels) {
		System.out.println("select previous");
		List<L> reverseLabels = new ArrayList<>(labels);
		Collections.reverse(reverseLabels);
		selectNext(reverseLabels);
	}

	private void selectNext(Collection<L> labels) {
		System.out.println("select next");
		boolean foundSelected = false;
		for (L label : labels) {
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

	private void deselect(L label) {
		model.removeTag(LabelEditorTag.SELECTED, label);
	}

	private void deselectAll() {
		model.removeTag(LabelEditorTag.SELECTED);
	}

	private void defocusAll() {
		model.removeTag(LabelEditorTag.MOUSE_OVER);
	}

	private void focus(L label) {
		model.addTag(LabelEditorTag.MOUSE_OVER, label);
	}
}
