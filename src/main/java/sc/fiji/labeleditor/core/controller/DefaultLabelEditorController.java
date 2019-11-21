package sc.fiji.labeleditor.core.controller;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;

import java.util.Set;

public class DefaultLabelEditorController<L> implements LabelEditorController<L> {

	@Parameter
	Context context;

	protected LabelEditorModel<L> model;
	protected LabelEditorView<L> view;
	protected LabelEditorInterface<L> interfaceInstance;

	@Override
	public void init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		this.model = model;
		this.view = view;
		if(interfaceInstance != null) {
			view.listeners().remove(interfaceInstance::onViewChange);
			model.tagging().listeners().remove(interfaceInstance::onTagChange);
		}
		this.interfaceInstance = interfaceInstance;
		if(context != null) context.inject(interfaceInstance);
		view.listeners().add(interfaceInstance::onViewChange);
		model.tagging().listeners().add(interfaceInstance::onTagChange);
	}

	@Override
	public void addDefaultBehaviours() {
		interfaceInstance.installBehaviours(model, this, view);
	}

	@Override
	public void triggerLabelingChange() {
		view.updateOnLabelingChange();
	}

	@Override
	public LabelEditorInterface<L> interfaceInstance() {
		return interfaceInstance;
	}

	@Override
	public void install(LabelEditorBehaviours behaviour) {
		if(context != null) context.inject(behaviour);
		behaviour.init(model, this, view);
		behaviour.install(interfaceInstance.behaviours(), interfaceInstance.getComponent());
	}

	@Override
	public IterableInterval<LabelingType<L>> labelingInScope() {
		return model.labeling();
	}

	@Override
	public Set<L> labelSetInScope() {
		return model.labeling().getMapping().getLabels();
	}

}
