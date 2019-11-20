package com.indago.labeleditor.core.view;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.colors.ColorChangedEvent;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.model.tagging.TagChangedEvent;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.listeners.Listeners;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class LabelEditorView<L> {

	private LabelEditorModel<L> model;
	private final LabelEditorRenderers renderers = new LabelEditorRenderers();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;
	private boolean showToolTip = true;
	private boolean showLabelsInToolTip = true;
	private boolean showTagsInToolTip = true;

	public LabelEditorView() {}

	public LabelEditorView(LabelEditorModel<L> model) {
		init(model);
	}

	public void init(LabelEditorModel<L> model) {
		this.model = model;
		if(model.labeling() == null) return;
		renderers.clear();
		renderers.init(model, this);
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

	public LabelEditorRenderers renderers() {
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

	public LabelEditorView<L> setShowToolTip(boolean showToolTip) {
		this.showToolTip = showToolTip;
		return this;
	}

	public LabelEditorView<L> setShowLabelsInToolTip(boolean showLabelsInToolTip) {
		this.showLabelsInToolTip = showLabelsInToolTip;
		return this;
	}

	public LabelEditorView<L> setShowTagsInToolTip(boolean showTagsInToolTip) {
		this.showTagsInToolTip = showTagsInToolTip;
		return this;
	}
}
