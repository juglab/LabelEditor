package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandle;
import bdv.util.BdvSource;
import bdv.viewer.ViewerPanel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.Intervals;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;
import sc.fiji.labeleditor.plugin.behaviours.FocusBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.PopupBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class BdvInterface<L> implements LabelEditorInterface<L> {

	@Parameter
	Context context;

	private final BdvHandle bdvHandle;
	private final List<BdvSource> sources;
	private final Behaviours behaviours;
	private final LabelEditorView<L> view;
	private boolean mode3D;
	private LabelingType<L> labelsAtCursor;

	public BdvInterface(BdvHandle bdvHandle, List<BdvSource> bdvSources, LabelEditorView view) {
		this.bdvHandle = bdvHandle;
		this.sources = bdvSources;
		this.view = view;
		this.behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor");
		behaviours.install(this.bdvHandle.getTriggerbindings(), "labeleditor");
	}

	public static <L> LabelEditorController control(LabelEditorModel<L> model, LabelEditorView<L> view, BdvHandle bdvHandle) {
		LabelEditorController<L> controller = new DefaultLabelEditorController<>();
		controller.init(model, view, new BdvInterface<>(bdvHandle, null, view));
		controller.addDefaultBehaviours();
		controller.interfaceInstance().set3DViewMode(false);
		return controller;
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	@Override
	public LabelingType<L> findLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model) {
		RandomAccess<LabelingType<L>> ra = model.labeling().randomAccess();
		Localizable pos = getDataPositionAtMouse();
		if(Intervals.contains(model.labeling(), pos)) {
			ra.setPosition(pos);
			if(labelsAtCursor != null && ra.get().getIndex().getInteger() == labelsAtCursor.getIndex().getInteger()) {
				return labelsAtCursor;
			}
			this.labelsAtCursor = ra.get();
			bdvHandle.getViewerPanel().getDisplay().setToolTipText(view.getToolTip(labelsAtCursor));
			return labelsAtCursor;
		}
		bdvHandle.getViewerPanel().getDisplay().setToolTipText(null);
		return null;
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition() {
		return labelsAtCursor;
	}

	private Localizable getDataPositionAtMouse() {
		//FIXME currently only works for 2D, 3D and 4D
		RealPoint mousePointer = new RealPoint(3);
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
		final int x = ( int ) mousePointer.getFloatPosition( 0 );
		final int y = ( int ) mousePointer.getFloatPosition( 1 );
		int time = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();
		if(mode3D) {
			final int z = ( int ) mousePointer.getFloatPosition( 2 );
			return new Point(x, y, z, time);
		}
		return new Point(x, y, time);
	}

	@Override
	public void installBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView<L> view) {
		install(model, controller, new SelectionBehaviours());
		install(model, controller, new FocusBehaviours<>());
		install(model, controller, new LabelingModificationBehaviours());
		install(model, controller, new PopupBehaviours());
	}

	private void install(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorBehaviours behavioursAdded) {
		if(context != null) context.inject(behavioursAdded);
		behavioursAdded.init(model, view, controller);
		behavioursAdded.install(behaviours, bdvHandle.getViewerPanel().getDisplay());
	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public Behaviours behaviours() {
		return behaviours;
	}

	@Override
	public ViewerPanel getComponent() {
		return bdvHandle.getViewerPanel();
	}

	@Override
	public void onTagChange(List<TagChangedEvent> tagChangedEvents) {
		if(labelsAtCursor == null) return;
		bdvHandle.getViewerPanel().getDisplay().setToolTipText(view.getToolTip(labelsAtCursor));
		showToolTip(bdvHandle.getViewerPanel().getDisplay());
	}

	public static void showToolTip(JComponent component) {
		java.awt.Point locationOnScreen = MouseInfo.getPointerInfo().getLocation();
		java.awt.Point locationOnComponent = new java.awt.Point(locationOnScreen);
		SwingUtilities.convertPointFromScreen(locationOnComponent, component);
		if (component.contains(locationOnComponent)) {
			ToolTipManager.sharedInstance().mouseMoved(
					new MouseEvent(component, -1, System.currentTimeMillis(), 0, locationOnComponent.x, locationOnComponent.y,
							locationOnScreen.x, locationOnScreen.y, 0, false, 0));
		}
	}
}
