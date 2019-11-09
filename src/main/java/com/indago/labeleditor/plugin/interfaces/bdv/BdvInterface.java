package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.core.controller.LabelEditorActions;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class BdvInterface<L> implements LabelEditorInterface<L> {
	private final BdvHandlePanel panel;
	private final List<BdvSource> sources;
	private boolean mode3D;

	public BdvInterface(BdvHandlePanel panel) {
		this(panel, null);
	}

	public BdvInterface(BdvHandlePanel panel, List<BdvSource> bdvSources) {
		this.panel = panel;
		this.sources = bdvSources;
	}

	public static <L> LabelEditorController control(BdvHandlePanel panel, DefaultLabelEditorModel<L> model, LabelEditorView<L> renderer) {
		LabelEditorController<L> actionHandler = new LabelEditorController<>();
		actionHandler.init(new BdvInterface(panel), model, renderer);
		actionHandler.addDefaultActionHandlers();
		actionHandler.set3DViewMode(false);
		return actionHandler;
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

	private Localizable getDataPositionAtMouse() {
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
	public List<LabelEditorActions> getAvailableActions(LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		List<LabelEditorActions> res = new ArrayList<>();
		//TODO find actions by annotation
		res.add(new BdvSelectionActions<>(panel, actionManager, model, renderer));
		return res;
	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		panel.getViewerPanel().requestRepaint();
	}

}
