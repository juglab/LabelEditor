package com.indago.labeleditor.action;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
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

public class BdvActionHandler<L> extends AbstractActionHandler<L> {

	private final BdvHandlePanel panel;

	public BdvActionHandler(BdvHandlePanel bdvHandlePanel, LabelEditorModel<L> model, RenderingManager<L> renderer) {
		super(model, renderer);
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

	@Override
	protected void updateLabelRendering() {
		renderer.update();
		panel.getViewerPanel().requestRepaint();
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e) {
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

}
