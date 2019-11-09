package com.indago.labeleditor.core.view;

import java.util.HashMap;

//TODO make coloring smarter, e.g. add border color, somehow extendable
//FIXME trigger tag change event for all modifying methods!!
public class LabelEditorColors extends HashMap<Object, LUTChannel> {

	private final LabelEditorView view;

	public LabelEditorColors(LabelEditorView view) {
		this.view = view;
	}

	public void put(Object tag, int color) {
		put(tag, new LUTChannel(color));
		view.updateRenderers();
	}

}
