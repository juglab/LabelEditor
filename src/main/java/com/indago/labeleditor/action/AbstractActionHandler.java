package com.indago.labeleditor.action;

import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class AbstractActionHandler<L> implements ActionHandler<L> {

	protected final LabelEditorModel<L> model;
	protected final LabelEditorRenderer renderer;
	protected LabelingType<L> currentLabels;
	protected int currentSegment = -1;
	protected boolean mode3D;

	public AbstractActionHandler(LabelEditorModel<L> model, LabelEditorRenderer renderer) {
		this.model = model;
		this.renderer = renderer;
	}

	public abstract void init();

	protected void handleMouseMove(MouseEvent e) {
		LabelingType<L> labels = getLabelsAtMousePosition(e);
		int intIndex;
		try {
			intIndex = labels.getIndex().getInteger();
		} catch(ArrayIndexOutOfBoundsException exc) {
			defocusAll();
			return;
		}
		if(intIndex == currentSegment) return;
		currentSegment = intIndex;
		new Thread(() -> {
			defocusAll();
			currentLabels = labels;
			labels.forEach(this::focus);
			updateLabelRendering();
		}).start();
	}

	protected void handleClick() {
		if (noLabelsAtMousePosition()) {
			deselectAll();
		} else {
			selectFirst(currentLabels);
		}
		updateLabelRendering();
	}

	protected abstract void updateLabelRendering();

	protected boolean noLabelsAtMousePosition() {
		return currentLabels == null || currentLabels.size() == 0;
	}

	protected void handleWheelRotation(double direction, boolean isHorizontal) {
		if(noLabelsAtMousePosition()) return;
		if(!anySelected(currentLabels))
			selectFirst(currentLabels);
		if ( !isHorizontal )
			if(direction > 0)
				selectNext(currentLabels);
			else
				selectPrevious(currentLabels);
	}

	@Override
	public abstract LabelingType<L> getLabelsAtMousePosition(MouseEvent e);

	@Override
	public void set3DViewMode(boolean mode3D) {
		this.mode3D = mode3D;
	}

	protected void selectFirst(LabelingType<L> currentLabels) {
		List<L> orderedLabels = new ArrayList<>(currentLabels);
		orderedLabels.sort(model::compare);
		deselectAll();
		select(orderedLabels.get(0));
	}

	protected boolean isSelected(L label) {
		return model.getTags(label).contains(LabelEditorTag.SELECTED);
	}

	protected boolean anySelected(LabelingType<L> labels) {
		return labels.stream().anyMatch(label -> model.getTags(label).contains(LabelEditorTag.SELECTED));
	}

	protected void select(L label) {
		model.addTag(LabelEditorTag.SELECTED, label);
	}

	protected void selectPrevious(LabelingType<L> labels) {
		System.out.println("select previous");
		List<L> reverseLabels = new ArrayList<>(labels);
		Collections.reverse(reverseLabels);
		selectNext(reverseLabels);
	}

	protected void selectNext(Collection<L> labels) {
		System.out.println("select next");
		boolean foundSelected = false;
		for (L label : labels) {
			if(isSelected(label)) {
				foundSelected = true;
				deselect(label);
			} else {
				if(foundSelected) {
					select(label);
					return;
				}
			}
		}
	}

	protected void deselect(L label) {
		model.removeTag(LabelEditorTag.SELECTED, label);
	}

	protected void deselectAll() {
		model.removeTag(LabelEditorTag.SELECTED);
	}

	protected void defocusAll() {
		model.removeTag(LabelEditorTag.MOUSE_OVER);
		currentLabels = null;
	}

	protected void focus(L label) {
		model.addTag(LabelEditorTag.MOUSE_OVER, label);
	}
}
