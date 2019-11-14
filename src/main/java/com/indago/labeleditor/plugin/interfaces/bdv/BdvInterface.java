package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import bdv.viewer.TimePointListener;
import com.indago.labeleditor.core.LabelEditorOptions;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import com.indago.labeleditor.plugin.behaviours.SelectionBehaviours;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;
import java.util.List;

public class BdvInterface<L> implements LabelEditorInterface<L>, TimePointListener {
	private final BdvHandlePanel panel;
	private final List<BdvSource> sources;
	private final Behaviours behaviours;
	private final LabelEditorView view;
	private boolean mode3D;

	public BdvInterface(BdvHandlePanel panel, List<BdvSource> bdvSources, LabelEditorView view) {
		this.panel = panel;
		this.sources = bdvSources;
		this.view = view;
		this.behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor");
		behaviours.install(panel.getTriggerbindings(), "labeleditor");
		panel.getViewerPanel().addTimePointListener( this );
	}

	public static <L> LabelEditorController control(DefaultLabelEditorModel<L> model, LabelEditorView<L> view, BdvHandlePanel panel) {
		LabelEditorController<L> controller = new LabelEditorController<>();
		controller.init(model, view, new BdvInterface<>(panel, null, view));
		controller.addDefaultBehaviours();
		controller.interfaceInstance().set3DViewMode(false);
		return controller;
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model) {
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
	public void installBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller) {
		SelectionBehaviours<L> selectionBehaviours = new SelectionBehaviours<>();
		selectionBehaviours.init(model, controller);
		selectionBehaviours.install(behaviours, panel.getViewerPanel().getDisplay());

	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		panel.getViewerPanel().requestRepaint();
	}

	@Override
	public Behaviours behaviours() {
		return behaviours;
	}

	@Override
	public Component getComponent() {
		return panel.getViewerPanel();
	}

	@Override
	public void timePointChanged(int timePointIndex) {
		view.updateTimePoint(timePointIndex);
	}
}
