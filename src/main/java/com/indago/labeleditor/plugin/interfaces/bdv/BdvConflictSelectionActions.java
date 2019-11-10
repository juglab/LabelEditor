package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.actions.ConflictSelectionActions;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class BdvConflictSelectionActions<L> extends ConflictSelectionActions<L> {

	private final BdvHandlePanel panel;

	public BdvConflictSelectionActions(BdvHandlePanel bdvHandlePanel, LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		super(model, renderer, actionManager);
		this.panel = bdvHandlePanel;
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
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "labeleditor");
		behaviours.install( panel.getBdvHandle().getTriggerbindings(), "labeleditor-conflictselection" );
		behaviours.behaviour(
				(ScrollBehaviour) (wheelRotation, isHorizontal, x, y) -> handleShiftWheelRotation(wheelRotation, isHorizontal),
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
