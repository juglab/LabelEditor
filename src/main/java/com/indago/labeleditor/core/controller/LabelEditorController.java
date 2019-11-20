package com.indago.labeleditor.core.controller;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;

import java.util.Set;

public interface LabelEditorController<L> {

	void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance);

	void addDefaultBehaviours();

	void triggerLabelingChange();

	LabelEditorInterface<L> interfaceInstance();

	void install(LabelEditorBehaviours behaviour);

	IterableInterval<LabelingType<L>> labelingInScope();

	Set<L> labelSetInScope();
}
