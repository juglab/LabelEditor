package sc.fiji.labeleditor.core;

import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Disposable;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

import java.awt.*;

public interface LabelEditorPanel extends Disposable {

	<L> InteractiveLabeling add(ImgLabeling<L, IntType> labels);

	<L> InteractiveLabeling add(ImgLabeling<L, IntType> labels, Img data);

	<L> InteractiveLabeling add(LabelEditorModel<L> model);

	<T extends IntegerType<T>> InteractiveLabeling addFromLabelMap(Img<T> indexImg);

	<T extends IntegerType<T>> InteractiveLabeling addFromLabelMap(Img data, Img<T> indexImg);

	Container get();

	//	setConfig(.yaml)
}
