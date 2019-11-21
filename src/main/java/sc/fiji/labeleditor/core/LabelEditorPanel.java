package sc.fiji.labeleditor.core;

import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Disposable;

import java.awt.*;

public interface LabelEditorPanel extends Disposable {

	void init(Img data);

	<L> void init(ImgLabeling<L, IntType> labels);

	<L> void init(ImgLabeling<L, IntType> labels, Img data);

	<L> void init(LabelEditorModel<L> model);

	<T extends IntegerType<T>> void initFromLabelMap(Img<T> indexImg);

	<T extends IntegerType<T>> void initFromLabelMap(Img data, Img<T> indexImg);

	LabelEditorView view();

	LabelEditorModel model();

	LabelEditorController control();

	Container get();

//	setConfig(.yaml)
}
