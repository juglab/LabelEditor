package sc.fiji.labeleditor.core.view;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Disposable;
import org.scijava.listeners.Listeners;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

public interface LabelEditorView<L> extends Disposable {

	void init(LabelEditorModel<L> model);

	void updateRenderers();

	void updateOnLabelingChange();

	LabelEditorRenderers renderers();

	Listeners< ViewChangeListener > listeners();

	String getToolTip(LabelingType<L> labels);

	void setShowToolTip(boolean showToolTip);

	void setShowLabelsInToolTip(boolean showLabelsInToolTip);

	void setShowTagsInToolTip(boolean showTagsInToolTip);
}
