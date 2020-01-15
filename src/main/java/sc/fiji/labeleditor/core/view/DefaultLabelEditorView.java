package sc.fiji.labeleditor.core.view;

import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.colors.ColorChangedEvent;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.plugin.renderers.BorderLabelEditorRenderer;
import sc.fiji.labeleditor.plugin.renderers.DefaultLabelEditorRenderer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DefaultLabelEditorView<L> implements LabelEditorView<L> {

	@Parameter
	protected Context context;

	private LabelEditorModel<L> model;
	private final List<LabelEditorRenderer<L>> renderers = new ArrayList<>();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private boolean showToolTip = true;
	private boolean showLabelsInToolTip = true;
	private boolean showTagsInToolTip = true;

	public DefaultLabelEditorView(LabelEditorModel<L> model) {
		this.model = model;
		model.tagging().listeners().add(this::onTagChange);
		model.colors().listeners().add(this::onColorChange);
		notifyListeners();
	}

	private void onColorChange(ColorChangedEvent colorChangedEvent) {
		updateRenderers();
	}

	private void onTagChange(List<TagChangedEvent> tagChangedEvent) {
		updateRenderers();
	}

	public void updateRenderers() {
		if(model == null || model.labeling() == null) return;
		renderers.forEach(renderer -> renderer.updateOnTagChange(model));
		notifyListeners();
	}

	public void updateOnLabelingChange() {
		if(model == null || model.labeling() == null) return;
		renderers.forEach(LabelEditorRenderer::updateOnLabelingChange);
		renderers.forEach(renderer -> renderer.updateOnTagChange(model));
		notifyListeners();
	}

	public List<LabelEditorRenderer<L>> renderers() {
		return renderers;
	}

	public Listeners< ViewChangeListener > listeners() {
		return listeners;
	}

	private void notifyListeners() {
		if(listenersPaused) return;
		listeners.list.forEach(listener -> listener.viewChanged(new ViewChangedEvent()));
	}

	public String getToolTip(LabelingType<L> labels) {
		StringBuilder res = new StringBuilder("<html>");
		for (Iterator<L> iterator = labels.iterator(); iterator.hasNext(); ) {
			L label = iterator.next();
			String text = getToolTip(label);
			if(text != null) {
				res.append(text);
				if(iterator.hasNext()) res.append("<br>");
			}
		}
		if(res.length() == 6) return null;
		return res.toString();
	}

	private String getToolTip(L label) {
		StringBuilder res = new StringBuilder();
		Set<Object> tags = new HashSet<>(model.tagging().getTags(label));
		boolean selected = tags.contains(LabelEditorTag.SELECTED);
		if(showLabelsInToolTip) {
			res.append(label);
		}
		if(showTagsInToolTip) {
			tags.removeAll(Arrays.asList(LabelEditorTag.values()));
			if (tags.size() != 0) {
				if(showLabelsInToolTip) res.append(": ");
				for (Iterator<Object> iter = tags.iterator(); iter.hasNext(); ) {
					res.append(iter.next());
					if(iter.hasNext()) res.append(", ");
				}
			}
		}
		if(res.length() > 0) {
			if(selected) {
				return "<b>" + res.toString() + "</b>";
			}
			return res.toString();
		}
		else return null;
	}

	public void setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
	}

	public void setShowLabelsInToolTip(boolean showLabelsInToolTip) {
		this.showLabelsInToolTip = showLabelsInToolTip;
	}

	public void setShowTagsInToolTip(boolean showTagsInToolTip) {
		this.showTagsInToolTip = showTagsInToolTip;
	}

	public void addDefaultRenderers() {
		renderers.clear();
		if(context == null) {
			add(new DefaultLabelEditorRenderer<>());
			add(new BorderLabelEditorRenderer<>());
		} else {
			context.getPluginIndex().get(LabelEditorRenderer.class).forEach(this::add);
		}
	}

	private void add(PluginInfo<?> renderer) {
		try {
			LabelEditorRenderer<L> instance = (LabelEditorRenderer<L>) renderer.createInstance();
			if (instance.canDisplay(model)) {
				add(instance);
			}
		} catch (InstantiableException e) {
			e.printStackTrace();
		}
	}

	private void add(LabelEditorRenderer<L> renderer) {
		prepare(renderer);
		renderers.add(renderer);
	}

	private void prepare(LabelEditorRenderer<L> renderer) {
		if(context != null) context.inject(renderer);
		renderer.init(model);
		renderer.updateOnTagChange(model);
	}

}
