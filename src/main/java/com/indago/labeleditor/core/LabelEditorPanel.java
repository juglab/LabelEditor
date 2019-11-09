package com.indago.labeleditor.core;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Disposable;

import java.awt.*;

public interface LabelEditorPanel<L> extends Disposable {

	void init(ImgPlus data);

	void init(ImgPlus data, ImgLabeling<L, IntType> labels);

	void init(ImgPlus data, LabelEditorModel<L> model);

	void init(ImgLabeling<L, IntType> labels);

	void init(LabelEditorModel<L> model);

	LabelEditorView<L> view();

	LabelEditorModel<L> model();

	LabelEditorController<L> control();

	Container get();

//	setConfig(.yaml)
}
