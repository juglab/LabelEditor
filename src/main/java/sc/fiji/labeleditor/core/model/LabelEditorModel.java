package sc.fiji.labeleditor.core.model;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.IntegerType;
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;

import java.util.Comparator;

public interface LabelEditorModel <L> {

	ImgLabeling<L, ? extends IntegerType<?>> labeling();
	LabelEditorTagging<L> tagging();
	LabelEditorTagColors colors();

	Comparator<Object> getTagComparator();
	Comparator<L> getLabelComparator();

	RandomAccessibleInterval<?> getData();

	String getName();
	void setName(String name);

	Listeners<LabelingChangeListener> labelingListeners();
	void pauseLabelingListeners();
	void resumeLabelingListeners();
	void notifyLabelingListeners();
}
