package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LabelEditorRenderers extends ArrayList<LabelEditorRenderer> {

	private LabelEditorModel model;
	private LabelEditorView view;

	public void init(LabelEditorModel model, LabelEditorView view) {
		this.model = model;
		this.view = view;
	}

	public void addDefaultRenderers() {
		//FIXME inject context, not create new one
		List<PluginInfo<?>> renderers = new Context().getPluginIndex().get(LabelEditorRenderer.class);
		renderers.forEach(renderer -> {
			try {
				add((LabelEditorRenderer) renderer.createInstance());
			} catch (InstantiableException e) {
				e.printStackTrace();
			}
		});
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
