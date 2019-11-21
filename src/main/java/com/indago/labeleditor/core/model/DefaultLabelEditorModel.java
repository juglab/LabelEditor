package com.indago.labeleditor.core.model;

import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

public class DefaultLabelEditorModel<L> extends AbstractLabelEditorModel<L> {

	public DefaultLabelEditorModel(ImgLabeling<L, IntType> labeling) {
		super(labeling);
		addDefaultColorsets();
	}

	public static DefaultLabelEditorModel<IntType> initFromLabelMap(Img labelMap) {
		return new DefaultLabelEditorModel<IntType>(makeLabeling(labelMap));
	}

	protected void addDefaultColorsets() {
		colors().getDefaultFaceColor().set(DefaultColors.defaultFace());
		colors().getDefaultBorderColor().set(DefaultColors.defaultBorder());
		colors().getSelectedFaceColor().set(DefaultColors.selectedFace());
		colors().getSelectedBorderColor().set(DefaultColors.selectedBorder());
		colors().getFocusFaceColor().set(DefaultColors.focusFace());
		colors().getFocusBorderColor().set(DefaultColors.focusBorder());
	}

}
