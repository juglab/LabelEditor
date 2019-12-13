package sc.fiji.labeleditor.core.model;

import de.embl.cba.table.select.SelectionModel;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColorset;
import sc.fiji.labeleditor.core.model.colors.LabelEditorTagColors;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTagging;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import java.util.Comparator;
import java.util.List;

public interface LabelEditorModel <L> {

	ImgLabeling<L, IntType> labeling();

	LabelEditorTagging<L> tagging();

	LabelEditorTagColors colors();

	List<LabelEditorColorset> getVirtualChannels();

	void setTagComparator(Comparator<Object> comparator);

	void setLabelComparator(Comparator<L> comparator);

	Comparator<Object> getTagComparator();
	Comparator<L> getLabelComparator();

	RandomAccessibleInterval getData();

	SelectionModel<L> getSelectionModel();
	void setSelectionModel(SelectionModel<L> model);
}
