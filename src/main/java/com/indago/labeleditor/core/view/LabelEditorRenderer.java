package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingMapping;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.util.Map;
import java.util.Set;

public interface LabelEditorRenderer<L> extends SciJavaPlugin {
	void init(LabelEditorModel model);
	void updateOnTagChange(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, LabelEditorTagColors tagColors);
	void updateOnLabelingChange();
	RandomAccessibleInterval getOutput();

	default String getName() {
		Plugin annotation = getClass().getAnnotation(Plugin.class);
		if(annotation != null) return annotation.name();
		return null;
	}

}
