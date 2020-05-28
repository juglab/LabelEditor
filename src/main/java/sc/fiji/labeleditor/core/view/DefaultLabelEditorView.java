/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.core.view;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.listeners.Listeners;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.PluginInfo;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelingChangedEvent;
import sc.fiji.labeleditor.core.model.colors.ColorChangedEvent;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.plugin.renderers.BorderLabelEditorRenderer;
import sc.fiji.labeleditor.plugin.renderers.DefaultLabelEditorRenderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultLabelEditorView<L> implements LabelEditorView<L> {

	@Parameter
	protected Context context;

	private LabelEditorModel<L> model;
	private final List<LabelEditorRenderer<L>> renderers = new ArrayList<>();
	private final Listeners.List<ViewChangeListener> listeners = new Listeners.SynchronizedList<>();
	private boolean listenersPaused = false;

	public DefaultLabelEditorView(LabelEditorModel<L> model) {
		this.model = model;
		model.tagging().listeners().add(this::onTagChange);
		model.colors().listeners().add(this::onColorChange);
		model.labelingListeners().add(this::onLabelingChange);
		notifyListeners();
	}

	private void onColorChange(ColorChangedEvent colorChangedEvent) {
		updateRenderers();
	}

	private void onTagChange(List<TagChangedEvent> tagChangedEvent) {
		updateRenderers();
	}

	public synchronized void updateRenderers() {
		if(model == null || model.labeling() == null) return;
		renderers.forEach(renderer -> {
			if(renderer.isActive()) renderer.updateOnTagChange(model);
		});
		notifyListeners();
	}

	private void onLabelingChange(LabelingChangedEvent e) {
		if(model == null || model.labeling() == null) return;
		renderers.forEach(renderer -> {
			if(renderer.isActive()) {
				renderer.updateOnLabelingChange();
				renderer.updateOnTagChange(model);
			}
		});
		notifyListeners();
	}

	public List<LabelEditorRenderer<L>> renderers() {
		return Collections.unmodifiableList(renderers);
	}

	public Listeners< ViewChangeListener > listeners() {
		return listeners;
	}

	private void notifyListeners() {
		if(listenersPaused) return;
		listeners.list.forEach(listener -> listener.viewChanged(new ViewChangedEvent()));
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

	public void add(LabelEditorRenderer<L> renderer) {
		prepare(renderer);
		renderers.add(renderer);
	}

	@Override
	public void setActive(LabelEditorRenderer<?> renderer, boolean active) {
		if(renderer.isActive() != active) {
			renderer.setActive(active);
			notifyListeners();
		}
	}

	private void prepare(LabelEditorRenderer<L> renderer) {
		if(context != null) context.inject(renderer);
		renderer.init(model);
		renderer.updateOnTagChange(model);
	}

}
