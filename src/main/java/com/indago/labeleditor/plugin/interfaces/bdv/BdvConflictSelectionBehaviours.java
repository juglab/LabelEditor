package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.ConflictSelectionBehaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class BdvConflictSelectionBehaviours<L> extends ConflictSelectionBehaviours<L> {

	public BdvConflictSelectionBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller, BdvHandlePanel panel) {
		super(model, controller);
		install( panel.getBdvHandle().getTriggerbindings(), "labeleditor-bdv" );
		MouseMotionListener mml = new MouseMotionListener() {
			@Override
			public void mouseDragged(MouseEvent e) {}
			@Override
			public void mouseMoved(MouseEvent e) {
				getMouseMoveBehaviour().move(e);
			}
		};
		panel.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}


}
