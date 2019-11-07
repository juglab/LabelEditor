package com.indago.labeleditor.action.bdv;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.ActionManager;
import com.indago.labeleditor.action.ViewerActionBridge;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BdvActionBridge<L> implements ViewerActionBridge<L> {
	private final BdvHandlePanel panel;
	private boolean mode3D;

	public BdvActionBridge(BdvHandlePanel panel) {
		this.panel = panel;
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model) {
		RandomAccess<LabelingType<L>> ra = model.labels().randomAccess();
		ra.setPosition(getDataPositionAtMouse());
		return ra.get();
	}

	@Override
	public Localizable getDataPositionAtMouse() {
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

	@Override
	public void update() {
		panel.getViewerPanel().requestRepaint();
	}

	@Override
	public List<ActionHandler<L>> getAvailableActions(ActionManager<L> actionManager, LabelEditorModel<L> model, RenderingManager<L> renderer) {
		List<ActionHandler<L>> res = new ArrayList<>();
		res.add(new BdvSelectionActions<>(panel, actionManager, model, renderer));
		return res;
	}

}
