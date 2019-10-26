package com.indago.labeleditor.model;

public class TagChangedEvent<L> {
	public Object tag;
	public L label;
	public Action action;

	public enum Action {
		REMOVED, ADDED
	}
}
