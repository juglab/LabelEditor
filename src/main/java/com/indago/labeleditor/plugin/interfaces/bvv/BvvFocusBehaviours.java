package com.indago.labeleditor.plugin.interfaces.bvv;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.behaviours.FocusBehaviours;
import net.imglib2.roi.labeling.LabelingType;

import java.util.List;

public class BvvFocusBehaviours<L> extends FocusBehaviours<L> {

	private BvvInterface<L> bvvInterface;

	public void init(LabelEditorModel<L> model, LabelEditorController<L> control, LabelEditorView<L> view, BvvInterface<L> bvvInterface) {
		super.init(model, control, view);
		this.bvvInterface = bvvInterface;
	}

	@Override
	public synchronized void focusFirstLabelAtPosition(int x, int y) {
		List<LabelingType<L>> allSets = bvvInterface.getAllLabelsAtMousePosition(x, y, model);
		if(allSets == null || allSets.size() == 0) {
			model.tagging().pauseListeners();
			defocus();
			model.tagging().resumeListeners();
			return;
		}
		LabelingType<L> labelset = allSets.get(0);
		int intIndex;
		try {
			intIndex = labelset.getIndex().getInteger();
		} catch(ArrayIndexOutOfBoundsException exc) {return;}
		if(intIndex == currentSegment) return;
		currentSegment = intIndex;
		new Thread(() -> {
			model.tagging().pauseListeners();
			defocus();
			focus(labelset);
			model.tagging().resumeListeners();
		}).start();
	}
}
