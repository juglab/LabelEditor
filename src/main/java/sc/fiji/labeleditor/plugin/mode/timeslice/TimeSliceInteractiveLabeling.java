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

import bdv.viewer.TimePointListener;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.controller.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;

import java.util.HashSet;
import java.util.Set;

public class TimeSliceInteractiveLabeling<L> extends DefaultInteractiveLabeling<L> {

	private long timePoint = 0;
	private Set<L> labelsInScope = new HashSet<>();
	private boolean processingLabelsInScope = false;

	public TimeSliceInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface interfaceInstance) {
		super(model, view, interfaceInstance);
	}

	@Override
	public void initialize() {
		super.initialize();
		try {
			BdvInterface bdv = (BdvInterface) interfaceInstance;
			bdv.getComponent().addTimePointListener(this::timePointChanged);
		} catch (ClassCastException e) {
			System.err.println("Cannot add a timepoint listener to interface " + interfaceInstance.getClass().getName());
		}
	}

	private void timePointChanged(int index) {
		this.timePoint = index;
		for (LabelEditorRenderer renderer : view().renderers()) {
			if(renderer instanceof TimePointListener) {
				((TimePointListener) renderer).timePointChanged(index);
			}
		}
		view().updateRenderers();
		new Thread(() -> {
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
	public RandomAccessibleInterval<LabelingType<L>> getLabelingInScope() {
		try {
			return ((TimeSliceLabelEditorModel<L>) model()).getLabelingAtTime(timePoint);
		} catch (ClassCastException e) {
			System.err.println("Model is no TimeSliceLabelEditorModel. Operation will be performed on the whole labeling instead of only one timepoint.");
		}
		return model().labeling();
	}

	@Override
	public Set<L> getLabelSetInScope() {
		while(processingLabelsInScope){}
		return labelsInScope;
	}
}
