package sc.fiji.labeleditor.plugin.behaviours;

import org.scijava.module.DefaultMutableModuleItem;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import org.scijava.command.Command;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Plugin(type = Command.class, name = "LabelEditor options", initializer = "initRendererList")
public class LabelEditorOptionsCommand<L> extends InteractiveCommand {

	@Parameter
	private LabelEditorView<L> view;

	@Parameter
	private LabelEditorInterface labelEditorInterface;

	private Map<String, LabelEditorRenderer<L>> namedRenderers;

	@Override
	public void run() {
		namedRenderers.forEach((name, renderer) -> {
			labelEditorInterface.setRendererActive(renderer, (Boolean)getInput(name));
		});
	}

	protected void initRendererList() {
		namedRenderers = new HashMap<>();
		view.renderers().forEach(renderer -> {
			namedRenderers.put(renderer.getName(), renderer);
			final DefaultMutableModuleItem<Boolean> item =
					new DefaultMutableModuleItem<>(this, renderer.getName(), Boolean.class);
			item.setPersisted(false);
			item.setValue(this, renderer.isActive());
			addInput(item);
		});
	}
}
