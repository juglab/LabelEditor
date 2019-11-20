package com.indago.labeleditor.plugin.renderers;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import org.scijava.plugin.Plugin;

@Plugin(type = LabelEditorRenderer.class, name = "labels", priority = 1)
public class DefaultLabelEditorRenderer<L> extends AbstractLabelEditorRenderer<L> {

	@Override
	public <M extends LabelEditorModel> boolean canDisplay(M model) {
		return DefaultLabelEditorModel.class.isAssignableFrom(model.getClass());
	}

}
