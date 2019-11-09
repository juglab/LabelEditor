package com.indago.labeleditor.plugin.bdv;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.plugin.actions.SelectionActions;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class BdvSelectionActions<L> extends SelectionActions<L> {

	private final BdvHandlePanel panel;

	public BdvSelectionActions(BdvHandlePanel bdvHandlePanel, LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		super(model, renderer, actionManager);
		this.panel = bdvHandlePanel;
		initMouseMotionListener();
		installBdvBehaviours();
	}

	private void initMouseMotionListener() {
		System.out.println("init mouse motion listener");
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
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "labeleditor");
		behaviours.install( panel.getBdvHandle().getTriggerbindings(), "labeleditor-defaults" );
		behaviours.behaviour(
				(ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> handleWheelRotation(wheelRotation, isHorizontal),
				"browse labels",
				"shift scroll" );
		behaviours.behaviour(
				(ClickBehaviour) (arg0, arg1) -> handleClick(),
				"select current label",
				"button1" );
		behaviours.behaviour(
				(ClickBehaviour) (arg0, arg1) -> handleShiftClick(),
				"add current label to selection",
				"shift button1" );
	}


}
