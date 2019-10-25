package com.indago.labeleditor.display;

import com.indago.labeleditor.LUTChannel;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imglib2.type.numeric.ARGBType;

import java.util.List;

public interface LUTBuilder<U> {
	/**
	 * @param model the model to display
	 * @return a lookup table containing {@link ARGBType} colors for displaying the model
	 */
	int[] build(LabelEditorModel model);

	default List<LUTChannel> getVirtualChannels() {
		return null;
	}
}
