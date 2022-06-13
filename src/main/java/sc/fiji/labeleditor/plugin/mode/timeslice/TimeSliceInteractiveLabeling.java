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
package sc.fiji.labeleditor.plugin.mode.timeslice;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;
import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Parameter;
import org.scijava.table.interactive.SelectionModel;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelingChangedEvent;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeSliceInteractiveLabeling<L> implements InteractiveLabeling<L> {

	@Parameter
	private
	DisplayService displayService;

	private final LabelEditorInterface interfaceInstance;
	private final boolean singleModelView;
	private int timePoint = 0;
	private Set<L> labelsInScope = new HashSet<>();
	private volatile boolean processingLabelsInScope = false;
	private List<LabelEditorModel<L>> models;
	private List<LabelEditorView<L>> views;
	private SelectionModel<L> selectionModel;

	public TimeSliceInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface interfaceInstance) {
		this.interfaceInstance = interfaceInstance;
		this.models = Collections.singletonList(model);
		this.views = Collections.singletonList(view);
		this.singleModelView = true;
	}

	public TimeSliceInteractiveLabeling(List<LabelEditorModel<L>> models, List<LabelEditorView<L>> views, LabelEditorInterface interfaceInstance) {
		this.models = models;
		this.views = views;
		this.interfaceInstance = interfaceInstance;
		this.singleModelView = false;
	}

	public void initialize() {
		interfaceInstance.installBehaviours(this);
		try {
			BdvInterface bdv = (BdvInterface) interfaceInstance;
			bdv.getComponent().addTimePointListener(this::timePointChanged);
		} catch (ClassCastException e) {
			System.err.println("Cannot add a timepoint listener to interface " + interfaceInstance.getClass().getName());
		}
		timePointChanged(0);
	}

	private void onLabelingChange(LabelingChangedEvent event) {
		if(displayService != null) {
			displayService.getDisplays(model().labeling().getIndexImg()).forEach(Display::update);
		}
	}

	private void timePointChanged(int index) {
		this.timePoint = index;
		views.forEach(view -> view.listeners().remove(interfaceInstance::onViewChange));
		models.forEach(model -> model.tagging().listeners().remove(interfaceInstance::onTagChange));
		models.forEach(model -> model.labelingListeners().remove(this::onLabelingChange));
		views.get(index).listeners().add(interfaceInstance::onViewChange);
		models.get(index).tagging().listeners().add(interfaceInstance::onTagChange);
		models.get(index).labelingListeners().add(this::onLabelingChange);
		if(singleModelView) new Thread(() -> {
			processingLabelsInScope = true;
			labelsInScope.clear();
			boolean[] setDone = new boolean[model().labeling().getMapping().numSets()];
			Cursor<LabelingType<L>> cursor = Views.iterable(getLabelingInScope()).cursor();
			while(cursor.hasNext()) {
				int val = cursor.next().getIndex().getInteger();
				if(setDone[val]) continue;
				setDone[val] = true;
				Set<L> labels = cursor.get();
				if(labels.size() == 0) continue;
				labelsInScope.addAll(labels);
			}
			processingLabelsInScope = false;
		}).start();
	}

	@Override
	public LabelEditorModel<L> model() {
		return singleModelView ? models.get(0) : models.get(timePoint);
	}

	@Override
	public LabelEditorView<L> view() {
		return singleModelView ? views.get(0) : views.get(timePoint);
	}

	@Override
	public LabelEditorInterface interfaceInstance() {
		return interfaceInstance;
	}

	@Override
	public RandomAccessibleInterval<LabelingType<L>> getLabelingInScope() {
		if(singleModelView) {
			try {
				return ((TimeSliceLabelEditorModel<L>) model()).getLabelingAtTime(timePoint);
			} catch (ClassCastException e) {
				System.err.println("Model is no TimeSliceLabelEditorModel. Operation will be performed on the whole labeling instead of only one timepoint.");
			}
		}
		return model().labeling();
	}

	@Override
	public Set<L> getLabelSetInScope() {
		if(singleModelView) {
			while(processingLabelsInScope){}
			return labelsInScope;
		} else {
			return models.get(timePoint).labeling().getMapping().getLabels();
		}
	}

	@Override
	public SelectionModel<L> getSelectionModel() {
		return selectionModel;
	}

	@Override
	public void setSelectionModel(SelectionModel<L> model) {
		this.selectionModel = model;
	}
}
