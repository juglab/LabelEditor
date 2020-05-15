package sc.fiji.labeleditor.core.view;

import net.imglib2.RandomAccessibleInterval;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

public interface LabelEditorRenderer<L> extends SciJavaPlugin {
	void init(LabelEditorModel<L> model);
	void updateOnTagChange(LabelEditorModel<L> model);
	void updateOnLabelingChange();
	void setActive(boolean active);
	boolean isActive();

	RandomAccessibleInterval getOutput();

	default String getName() {
		Plugin annotation = getClass().getAnnotation(Plugin.class);
		if(annotation != null) return annotation.name();
		return null;
	}

	<M extends LabelEditorModel> boolean canDisplay(M model);
}
