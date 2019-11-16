package com.indago.labeleditor.core.model.tagging;

import java.util.List;

public interface TagChangeListener {
	void tagChanged(List<TagChangedEvent> e);
}
