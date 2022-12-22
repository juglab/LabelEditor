/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2022 Deborah Schmidt
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
package sc.fiji.labeleditor.core.controller;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Context;
import org.scijava.Initializable;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.table.interactive.SelectionModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelingChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import java.util.Set;

public class DefaultInteractiveLabeling<L> implements InteractiveLabeling<L> {

	@Parameter
	DisplayService displayService;

	protected final LabelEditorInterface interfaceInstance;
	private final LabelEditorModel<L> model;
	private final LabelEditorView<L> view;
	private SelectionModel<L> selectionModel;

	public DefaultInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface interfaceInstance) {
		this.model = model;
		this.view = view;
		this.interfaceInstance = interfaceInstance;
	}

	@Override
	public LabelEditorModel<L> model() {
		return model;
	}

	@Override
	public LabelEditorView<L> view() {
		return view;
	}

	public void initialize() {
		view.listeners().remove(interfaceInstance::onViewChange);
		model.tagging().listeners().remove(interfaceInstance::onTagChange);
		view.listeners().add(interfaceInstance::onViewChange);
		model.tagging().listeners().add(interfaceInstance::onTagChange);
		model.labelingListeners().add(this::onLabelingChange);
		interfaceInstance.installBehaviours(this);
	}

	private void onLabelingChange(LabelingChangedEvent event) {
		if(displayService != null) {
			displayService.getDisplays(model().labeling().getIndexImg()).forEach(Display::update);
		}
	}

	@Override
	public LabelEditorInterface interfaceInstance() {
		return interfaceInstance;
	}

	@Override
	public RandomAccessibleInterval<LabelingType<L>> getLabelingInScope() {
		return model().labeling();
	}

	@Override
	public Set<L> getLabelSetInScope() {
		return model().labeling().getMapping().getLabels();
	}

	@Override
	public SelectionModel<L> getSelectionModel() {
		return selectionModel;
	}

	@Override
	public void setSelectionModel(SelectionModel<L> model) {
		this.selectionModel = model;
	}

	@Override
	public String toString() {
		return model().getName();
	}
}
