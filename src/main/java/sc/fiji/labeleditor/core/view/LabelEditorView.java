package sc.fiji.labeleditor.core.view;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Disposable;
import org.scijava.listeners.Listeners;

import java.util.List;

public interface LabelEditorView<L> extends Disposable {

	void updateRenderers();

	void updateOnLabelingChange();

	List< LabelEditorRenderer<L> > renderers();

	Listeners< ViewChangeListener > listeners();

	String getToolTip(LabelingType<L> labels);

	void setShowToolTip(boolean showToolTip);

	void setShowLabelsInToolTip(boolean showLabelsInToolTip);

	void setShowTagsInToolTip(boolean showTagsInToolTip);

	void addDefaultRenderers();
}
