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

import bdv.util.BdvOptions;
import org.scijava.Context;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import java.util.ArrayList;
import java.util.List;

public class TimeSliceLabelEditorBdvPanel extends LabelEditorBdvPanel {

	public TimeSliceLabelEditorBdvPanel() {
		super();
	}

	public TimeSliceLabelEditorBdvPanel(Context context) {
		super(context);
	}

	public TimeSliceLabelEditorBdvPanel(BdvOptions options) {
		super(options);
	}

	public TimeSliceLabelEditorBdvPanel(Context context, BdvOptions options) {
		super(context, options);
	}

	@Override
	public <L> InteractiveLabeling<L> add(LabelEditorModel<L> model, LabelEditorView<L> view, BdvOptions options) {
		TimeSliceInteractiveLabeling<L> interactiveLabeling = new TimeSliceInteractiveLabeling<>(model, view, getInterfaceInstance());
		if(context() != null) context().inject(interactiveLabeling);
		interactiveLabeling.initialize();
		getInterfaceInstance().display(interactiveLabeling, options);
		return interactiveLabeling;
	}

	public <L> InteractiveLabeling<L> add(List<LabelEditorModel<L>> models) {
		return add(models, new BdvOptions());
	}

	public <L> InteractiveLabeling<L> add(List<LabelEditorModel<L>> models, BdvOptions options) {
		List<LabelEditorView<L>> views = new ArrayList<>();
		for (LabelEditorModel<L> model : models) {
			DefaultLabelEditorView<L> view = new DefaultLabelEditorView<>(model);
			if(context() != null) context().inject(view);
			view.addDefaultRenderers();
			views.add(view);
		}
		return add(models, views, options);
	}

	public <L> InteractiveLabeling<L> add(List<LabelEditorModel<L>> models, List<LabelEditorView<L>> views) {
		return add(models, views, new BdvOptions());
	}

	public <L> InteractiveLabeling<L> add(List<LabelEditorModel<L>> models, List<LabelEditorView<L>> views, BdvOptions options) {
		TimeSliceInteractiveLabeling<L> interactiveLabeling = new TimeSliceInteractiveLabeling(models, views, getInterfaceInstance());
		if(context() != null) context().inject(interactiveLabeling);
		interactiveLabeling.initialize();
		getInterfaceInstance().display(interactiveLabeling, options);
		return interactiveLabeling;
	}
}
