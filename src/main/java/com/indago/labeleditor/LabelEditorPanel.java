package com.indago.labeleditor;

import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.awt.*;

public interface LabelEditorPanel<L> {

	void init(ImgPlus data);

	void init(ImgPlus data, ImgLabeling<L, IntType> labels);

	void init(ImgPlus data, LabelEditorModel<L> model);

	void init(ImgLabeling<L, IntType> labels);

	void init(LabelEditorModel<L> model);

	void updateLabelRendering();

	LabelEditorRenderer<L> getRenderer();

	LabelEditorModel<L> getModel();

	ActionHandler<L> getActionHandler();

	Container get();

	void updateData(ImgPlus<L> imgPlus);

//	setConfig(.yaml)
}
