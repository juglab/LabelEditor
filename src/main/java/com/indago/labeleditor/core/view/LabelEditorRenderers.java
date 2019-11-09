package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.renderer.BorderLabelEditorRenderer;
import com.indago.labeleditor.plugin.renderer.DefaultLabelEditorRenderer;

import java.util.ArrayList;
import java.util.Optional;

public class LabelEditorRenderers extends ArrayList<LabelEditorRenderer> {

	private LabelEditorModel model;
	private LabelEditorView view;

	public void init(LabelEditorView view, LabelEditorModel model) {
		this.view = view;
		this.model = model;
	}

	public void addDefaultRenderers() {
		//TODO find available renderers by annotation
		add(new DefaultLabelEditorRenderer<>());
		add(new BorderLabelEditorRenderer<>());
	}

	public Optional<LabelEditorRenderer> get(String name) {
		return stream().filter(renderer -> renderer.getName().equals(name)).findFirst();
	}

	@Override
	public LabelEditorRenderer set(int i, LabelEditorRenderer renderer) {
		prepare(renderer);
		return super.set(i, renderer);
	}

	@Override
	public boolean add(LabelEditorRenderer renderer) {
		prepare(renderer);
		return super.add(renderer);
	}

	@Override
	public void add(int i, LabelEditorRenderer renderer) {
		prepare(renderer);
		super.add(i, renderer);
	}

	private void prepare(LabelEditorRenderer renderer) {
		renderer.init(model.labels());
		renderer.updateOnTagChange(model.labels().getMapping(), model.tagging().get(), view.colors());
	}
}
