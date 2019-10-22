package com.indago.labeleditor;

public class LabelEditorTag {
	private String name;

	public LabelEditorTag(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		return name.equals(((LabelEditorTag)o).name);
	}
}
