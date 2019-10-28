package com.indago.labeleditor.action;

import com.indago.labeleditor.LabelEditorBvvPanel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultBvvActionHandler<L> implements ActionHandler<L> {

	private final LabelEditorBvvPanel<L, ?> panel;
	private final LabelEditorModel<L> model;
	private LabelingType<L> currentLabels;
	private int currentSegment;
	private boolean mode3D;

	public DefaultBvvActionHandler(LabelEditorBvvPanel<L, ?> panel, LabelEditorModel<L> model) {
		this.panel = panel;
		this.model = model;
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

		panel.getBvvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	private void installBdvBehaviours() {
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( panel.getBvvHandle().getTriggerbindings(), "my-new-behaviours" );
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
		List<LabelingType<L>> allSets = getAllLabelsAtMousePosition(e);
		if(allSets == null || allSets.size() == 0) {
			defocusAll();
			return;
		}
		LabelingType<L> labelset = allSets.get(0);
//		for (LabelingType<L> labelset : labels) {
			int intIndex;
			try {
				intIndex = labelset.getIndex().getInteger();
			} catch(ArrayIndexOutOfBoundsException exc) {return;}
			if(intIndex == currentSegment) return;
			currentSegment = intIndex;
			new Thread(() -> {
				defocusAll();
				currentLabels = labelset;
				labelset.forEach(this::focus);
				panel.updateLabelRendering();
			}).start();
//		}
	}

	private void handleClick() {
		if (noLabelsAtMousePosition()) {
			deselectAll();
		} else {
			selectFirst(currentLabels);
		}
		panel.updateLabelRendering();
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
	
	public List<LabelingType<L>> getAllLabelsAtMousePosition(MouseEvent e) {
//		Point pos = getMousePositionInBDV(e);
//		FIXME find all labels at this position but in all depths (depending on viewport)
//		return getLabelsAtPosition(pos);
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV);
	}
	
	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e) {
//		Point pos = getMousePositionInBDV(e);
//		return getLabelsAtPosition(pos);
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV).get(0);
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	private Point getMousePositionInBDV(MouseEvent e) {
		RealPoint gPos = new RealPoint(3);
		assert gPos.numDimensions() == 3;
		final RealPoint lPos = new RealPoint( 3 );
		lPos.setPosition(e.getX(), 0);
		lPos.setPosition(e.getY(), 1);
		AffineTransform3D transform = new AffineTransform3D();
		panel.getBvvHandle().getViewerPanel().getState().getViewerTransform(transform);
		transform.applyInverse( gPos, lPos );
		final int x = ( int ) gPos.getFloatPosition( 0 );
		final int y = ( int ) gPos.getFloatPosition( 1 );
		final int z = ( int ) gPos.getFloatPosition( 2 );
		int time = panel.getBvvHandle().getViewerPanel().getState().getCurrentTimepoint();
		return new Point(x, y, z, time);
	}

	private Set<LabelingType<L>> getLabelsAtMousePositionInBVV(MouseEvent e) {
		RealPoint gPos = new RealPoint(3);
		assert gPos.numDimensions() == 3;
		final RealPoint lPos = new RealPoint( 3 );
		lPos.setPosition(e.getX(), 0);
		lPos.setPosition(e.getY(), 1);
		AffineTransform3D transform = new AffineTransform3D();

		int time = panel.getBvvHandle().getViewerPanel().getState().getCurrentTimepoint();
		Set<LabelingType<L>> labels = new HashSet<>();
		for (int i = 0; i < 500; i++) {
			lPos.setPosition(i, 2);
			panel.getBvvHandle().getViewerPanel().getState().getViewerTransform(transform);
			transform.applyInverse( gPos, lPos );
			final int x = ( int ) gPos.getFloatPosition( 0 );
			final int y = ( int ) gPos.getFloatPosition( 1 );
			final int z = ( int ) gPos.getFloatPosition( 2 );
			Point pos = new Point(x, y, z, time);
			LabelingType<L> labelsAtPosition = getLabelsAtPosition(pos);
			if(labelsAtPosition != null && labelsAtPosition.size() > 0) {
				labels.add(labelsAtPosition);
			}
		}
		return labels;
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
