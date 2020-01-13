package sc.fiji.labeleditor.core.view;

import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import sc.fiji.labeleditor.core.model.LabelEditorModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LabelEditorRenderers extends ArrayList<LabelEditorRenderer> implements Disposable {

	private LabelEditorModel model;

	@Parameter
	Context context;

	boolean contextCreated = false;

	public void init(LabelEditorModel model) {
		this.model = model;
	}

	public void addDefaultRenderers() {
		clear();
		if(context == null) {
			context = new Context(true);
			contextCreated = true;
		}
		List<PluginInfo<?>> renderers = discoverRenderers();
		renderers.forEach(this::addRenderer);
	}

	private List<PluginInfo<?>> discoverRenderers() {
		return context.getPluginIndex().get(LabelEditorRenderer.class);
	}

	public Optional<LabelEditorRenderer> get(String name) {
		return stream().filter(renderer -> renderer.getName().equals(name)).findFirst();
	}

	@Override
	public LabelEditorRenderer set(int i, LabelEditorRenderer renderer) {
		prepare(renderer);
		return super.set(i, renderer);
	}

	private void addRenderer(PluginInfo<?> renderer) {
		try {
			LabelEditorRenderer instance = (LabelEditorRenderer) renderer.createInstance();
			if (instance.canDisplay(model)) {
				add(instance);
			}
		} catch (InstantiableException e) {
			e.printStackTrace();
		}
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
		if(context != null) context.inject(renderer);
		renderer.init(model);
		renderer.updateOnTagChange(model);
	}

	@Override
	public void dispose() {
		if(contextCreated) {
			context.dispose();
		}
	}
}
