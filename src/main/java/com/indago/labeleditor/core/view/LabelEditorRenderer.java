package com.indago.labeleditor.core.view;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingMapping;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.util.Map;
import java.util.Set;

public interface LabelEditorRenderer<L> extends SciJavaPlugin {
	void init(ImgLabeling<L, IntType> labels);
	void updateOnTagChange(LabelingMapping<L> mapping, Map<L, Set<Object>> tags, LabelEditorTagColors tagColors);
	void updateOnLabelingChange();
	RandomAccessibleInterval getOutput();

	default String getName() {
		Plugin annotation = getClass().getAnnotation(Plugin.class);
		if(annotation != null) return annotation.name();
		return null;
	}

}
