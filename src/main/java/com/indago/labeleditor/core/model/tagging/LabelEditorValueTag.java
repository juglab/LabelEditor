package com.indago.labeleditor.core.model.tagging;

import net.imglib2.type.numeric.RealType;

public class LabelEditorValueTag<T extends RealType<T>> {
	private T value;
	private String name;

	public LabelEditorValueTag(String name, T value) {
		setName(name);
		setValue(value);
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getIdentifier() {
		return name;
	}
}
