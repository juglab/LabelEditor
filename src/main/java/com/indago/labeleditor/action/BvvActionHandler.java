package com.indago.labeleditor.action;

import bvv.util.BvvHandle;
import com.indago.labeleditor.display.LabelEditorRenderer;
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

public class BvvActionHandler<L> extends AbstractActionHandler<L> {

	private final BvvHandle panel;

	public BvvActionHandler(BvvHandle panel, LabelEditorModel<L> model, LabelEditorRenderer renderer) {
		super(model, renderer);
		this.panel = panel;
	}

	@Override
	public void init() {
		initMouseMotionListener();
		installBvvBehaviours();
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

		panel.getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	private void installBvvBehaviours() {
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( panel.getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				(ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> handleWheelRotation(wheelRotation, isHorizontal),
				"browse segments",
				"shift scroll" );
		behaviours.behaviour(
				(ClickBehaviour) (arg0, arg1) -> handleClick(),
				"select current segment",
				"button1" );
	}

	@Override
	protected void handleMouseMove(MouseEvent e) {
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
				updateLabelRendering();
			}).start();
//		}
	}

	@Override
	protected void updateLabelRendering() {
		System.out.println("update");
		renderer.update();
		panel.getViewerPanel().requestRepaint();
	}

	private List<LabelingType<L>> getAllLabelsAtMousePosition(MouseEvent e) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV);
	}
	
	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV).get(0);
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
				try {
					LabelingType<L> labelsAtPosition = getLabelsAtPosition(pos);
					if(labelsAtPosition != null && labelsAtPosition.size() > 0) {
						labels.add(labelsAtPosition);
					}
				} catch(ArrayIndexOutOfBoundsException ignored) {}
		}
		return labels;
	}

	private LabelingType<L> getLabelsAtPosition(Localizable pos) {
		RandomAccess<LabelingType<L>> ra = model.getLabels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

}
