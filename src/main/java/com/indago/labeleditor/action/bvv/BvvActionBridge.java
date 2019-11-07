package com.indago.labeleditor.action.bvv;

import bvv.util.BvvSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.ActionManager;
import com.indago.labeleditor.action.ViewerActionBridge;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import org.apache.commons.lang.NotImplementedException;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BvvActionBridge<L> implements ViewerActionBridge<L> {
	private final BvvSource panel;

	public BvvActionBridge(BvvSource panel) {
		this.panel = panel;
	}

	protected List<LabelingType<L>> getAllLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV);
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV).get(0);
	}

	@Override
	public Localizable getDataPositionAtMouse() {
		throw new NotImplementedException();
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

	private Set<LabelingType<L>> getLabelsAtMousePositionInBVV(MouseEvent e, LabelEditorModel<L> model) {
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
				LabelingType<L> labelsAtPosition = getLabelsAtPosition(pos, model);
				if(labelsAtPosition != null && labelsAtPosition.size() > 0) {
					labels.add(labelsAtPosition);
				}
			} catch(ArrayIndexOutOfBoundsException ignored) {}
		}
		return labels;
	}

	private LabelingType<L> getLabelsAtPosition(Localizable pos, LabelEditorModel<L> model) {
		RandomAccess<LabelingType<L>> ra = model.labels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

	@Override
	public void set3DViewMode(boolean mode3D) {

	}

	@Override
	public void update() {
		panel.getBvvHandle().getViewerPanel().requestRepaint();
	}

	@Override
	public List<ActionHandler<L>> getAvailableActions(ActionManager<L> actionManager, LabelEditorModel<L> model, RenderingManager<L> renderer) {
		List<ActionHandler<L>> res = new ArrayList<>();
		res.add(new BvvSelectionActions<L>(panel.getBvvHandle(), actionManager, model, renderer, this));
		return res;
	}
}
