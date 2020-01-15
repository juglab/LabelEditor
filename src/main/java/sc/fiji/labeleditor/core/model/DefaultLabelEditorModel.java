package sc.fiji.labeleditor.core.model;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

public class DefaultLabelEditorModel<L> extends AbstractLabelEditorModel<L> {

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labeling, RandomAccessibleInterval data) {
		super(labeling);
		setData(data);
	}

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labeling) {
		super(labeling);
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval labelMap) {
		return new DefaultLabelEditorModel<IntType>(makeLabeling(labelMap));
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(RandomAccessibleInterval labelMap, RandomAccessibleInterval data) {
		return new DefaultLabelEditorModel<IntType>(makeLabeling(labelMap), data);
	}

}
