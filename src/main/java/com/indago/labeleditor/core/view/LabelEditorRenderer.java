package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

public interface LabelEditorRenderer<L> extends SciJavaPlugin {
	void init(LabelEditorModel model);
	void updateOnTagChange(LabelEditorModel model);
	void updateOnLabelingChange();

	RandomAccessibleInterval getOutput();

	default String getName() {
		Plugin annotation = getClass().getAnnotation(Plugin.class);
		if(annotation != null) return annotation.name();
		return null;
	}

	<M extends LabelEditorModel> boolean canDisplay(M model);
}
