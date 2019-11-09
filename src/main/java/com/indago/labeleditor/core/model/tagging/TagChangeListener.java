package com.indago.labeleditor.core.model.tagging;

public interface TagChangeListener<L> {
	void tagChanged(TagChangedEvent e);
}
