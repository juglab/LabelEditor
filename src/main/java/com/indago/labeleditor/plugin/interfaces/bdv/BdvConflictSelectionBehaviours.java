package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.behaviours.ConflictSelectionBehaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class BdvConflictSelectionBehaviours<L> extends ConflictSelectionBehaviours<L> {

	public BdvConflictSelectionBehaviours(BdvHandlePanel panel, LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer) {
		super(model, renderer, actionManager);
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
