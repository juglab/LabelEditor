package com.indago.labeleditor.core.model;

public class DefaultLabelEditorModel<L> extends AbstractLabelEditorModel<L> {

	public DefaultLabelEditorModel() {
		addDefaultColorsets();
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
