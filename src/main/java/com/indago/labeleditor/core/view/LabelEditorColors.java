package com.indago.labeleditor.core.view;

import java.util.HashMap;

public class LabelEditorColors extends HashMap<Object, LUTChannel> {

	public void put(Object tag, int color) {
		put(tag, new LUTChannel(color));
	}

}
