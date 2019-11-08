package com.indago.labeleditor.plugin.bvv;

import bvv.util.BvvHandle;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.plugin.actions.SelectionActions;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.ScrollBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class BvvSelectionActions<L> extends SelectionActions<L> {

	private final BvvInterface<L> bvvInterface;

	public BvvSelectionActions(LabelEditorController<L> actionManager, LabelEditorModel<L> model, LabelEditorView<L> renderer, BvvInterface<L> bvvInterface) {
		super(model, renderer, actionManager);
		this.bvvInterface = bvvInterface;
		initMouseMotionListener();
		installBvvBehaviours();
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

		bvvInterface.getBvvHandle().getViewerPanel().getDisplay().addMouseMotionListener( mml );
	}

	private void installBvvBehaviours() {
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( bvvInterface.getBvvHandle().getTriggerbindings(), "my-new-behaviours" );
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
	protected void handleMouseMove(MouseEvent e) {
		List<LabelingType<L>> allSets = bvvInterface.getAllLabelsAtMousePosition(e, model);
		if(allSets == null || allSets.size() == 0) {
			defocusAll();
			updateLabelRendering();
			return;
		}
		LabelingType<L> labelset = allSets.get(0);
//		for (LabelingType<L> labelset : labels) {
			int intIndex;
			try {
				intIndex = labelset.getIndex().getInteger();
			} catch(ArrayIndexOutOfBoundsException exc) {return;}
			if(intIndex == currentSegment) return;
			currentSegment = intIndex;
			new Thread(() -> {
				defocusAll();
				currentLabels = labelset;
				labelset.forEach(this::focus);
				updateLabelRendering();
			}).start();
//		}
	}
}
