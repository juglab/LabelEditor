package com.indago.labeleditor.core;

import com.indago.labeleditor.core.action.ActionManager;
import com.indago.labeleditor.core.action.ViewerInstance;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.display.RenderingManager;
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

	RenderingManager<L> rendering();

	LabelEditorModel<L> model();

	ActionManager<L> action();

	ViewerInstance<L> viewer();

	Container get();

	void updateData(ImgPlus<L> imgPlus);

//	setConfig(.yaml)
}
