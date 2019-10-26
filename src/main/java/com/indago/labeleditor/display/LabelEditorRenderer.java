package com.indago.labeleditor.display;

import com.indago.labeleditor.display.LUTChannel;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.List;

public interface LabelEditorRenderer<L> {
	void update();
	RandomAccessibleInterval<ARGBType> getRenderedLabels();
	default List<LUTChannel> getVirtualChannels() {
		return null;
	}
	default void setTagColor(Object tag, int color) {}
	default void removeTagColor(Object tag) {}
	int[] getLUT();
	LabelEditorModel<L> getModel();
}
