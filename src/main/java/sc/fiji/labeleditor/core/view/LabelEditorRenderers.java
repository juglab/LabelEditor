package sc.fiji.labeleditor.core.view;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LabelEditorRenderers extends ArrayList<LabelEditorRenderer> {

	private LabelEditorModel model;
	private LabelEditorView view;

	@Parameter
	Context context;

	public void init(LabelEditorModel model, LabelEditorView view) {
		this.model = model;
		this.view = view;
	}

	public void addDefaultRenderers() {
		clear();
		if(context == null) {
			context = new Context();
		}
		List<PluginInfo<?>> renderers = context.getPluginIndex().get(LabelEditorRenderer.class);
		renderers.sort((p1, p2) -> (int) (p1.getAnnotation().priority() - p2.getAnnotation().priority()));
		renderers.forEach(renderer -> {
			try {
				LabelEditorRenderer instance = (LabelEditorRenderer) renderer.createInstance();
				if(instance.canDisplay(model)) {
					add(instance);
				}
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
		if(context != null) context.inject(renderer);
		renderer.init(model);
		renderer.updateOnTagChange(model);
	}
}
