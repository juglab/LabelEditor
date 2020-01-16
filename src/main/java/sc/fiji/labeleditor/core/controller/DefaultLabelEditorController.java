package sc.fiji.labeleditor.core.controller;

import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import sc.fiji.labeleditor.core.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.util.Set;

public class DefaultLabelEditorController<L> implements LabelEditorController<L> {

	@Parameter
	Context context;

	protected LabelEditorInterface<L> interfaceInstance;

	protected InteractiveLabeling<L> labeling;

	@Override
	public InteractiveLabeling init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		labeling = new DefaultInteractiveLabeling<>(model, view, this);
		if(interfaceInstance != null) {
			view.listeners().remove(interfaceInstance::onViewChange);
			model.tagging().listeners().remove(interfaceInstance::onTagChange);
			this.interfaceInstance = interfaceInstance;
			if(context != null) context.inject(interfaceInstance);
			view.listeners().add(interfaceInstance::onViewChange);
			model.tagging().listeners().add(interfaceInstance::onTagChange);
			interfaceInstance.display(view);
		}
		return labeling;
	}

	@Override
	public LabelEditorInterface<L> interfaceInstance() {
		return interfaceInstance;
	}

	@Override
	public void install(LabelEditorBehaviours behaviour) {
		if(context != null) context.inject(behaviour);
		behaviour.init(labeling);
		behaviour.install(interfaceInstance.behaviours(), interfaceInstance.getComponent());
	}

	@Override
	public IterableInterval<LabelingType<L>> labelingInScope() {
		return labeling.model().labeling();
	}

	@Override
	public Set<L> labelSetInScope() {
		return labeling.model().labeling().getMapping().getLabels();
	}

}
