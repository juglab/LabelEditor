package com.indago.labeleditor.core.view;

import java.util.HashMap;

public class LabelEditorTagColors extends HashMap<Object, LabelEditorColorset> {

	private final LabelEditorView view;

	public LabelEditorTagColors(LabelEditorView view) {
		this.view = view;
	}

	public LabelEditorColorset get(Object tag) {
		return computeIfAbsent(tag, k -> new LabelEditorColorset(view));
	}
}
