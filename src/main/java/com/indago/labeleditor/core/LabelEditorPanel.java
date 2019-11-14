package com.indago.labeleditor.core;

import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Disposable;

import java.awt.*;

public interface LabelEditorPanel<L> extends Disposable {

	void init(Img data);

	void init(ImgLabeling<L, IntType> labels);

	void init(ImgLabeling<L, IntType> labels, Img data);

	void init(LabelEditorModel<L> model);

	void initFromIndexImage(Img indexImg);

	void initFromIndexImage(Img data, Img indexImg);

	LabelEditorView<L> view();

	LabelEditorModel<L> model();

	LabelEditorController<L> control();

	Container get();

//	setConfig(.yaml)
}
