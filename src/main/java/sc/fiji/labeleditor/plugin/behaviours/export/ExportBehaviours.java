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
package sc.fiji.labeleditor.plugin.behaviours.export;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.table.interactive.InteractiveTableDisplayViewer;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.plugin.table.LabelEditorTable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExportBehaviours implements LabelEditorBehaviours {

	@Parameter
	UIService ui;

	@Parameter
	Context context;

	private InteractiveLabeling interactiveLabeling;

	@Override
	public void init(InteractiveLabeling labeling) {
		this.interactiveLabeling = labeling;
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ExportLabels getExportSelectedLabels() {
		ExportLabels exportLabels = new ExportLabels(interactiveLabeling.model());
		context.inject(exportLabels);
		return exportLabels;
	}

	public ClickBehaviour getExportIndexImgBehaviour() {
		return (arg0, arg1) -> showIndexImg();
	}

	public ClickBehaviour getExportLabelMapBehaviour() {
		return (arg0, arg1) -> showLabelMap();
	}

	public ClickBehaviour getExportSourceImgBehaviour() {
		return (arg0, arg1) -> showData();
	}

	public ClickBehaviour getExportRendererBehaviour(LabelEditorRenderer renderer) {
		return (arg0, arg1) -> showRenderer(renderer);
	}

	public ClickBehaviour getExportTableBehaviour() {
		return (arg0, arg1) -> showTables();
	}

	public void showIndexImg() {
		show(interactiveLabeling.model().labeling().getIndexImg());
	}

	public void showLabelMap() {
		show(getLabelMap());
	}

	public <T extends RealType<T>> RandomAccessibleInterval<IntType> getLabelMap() {
		RandomAccessibleInterval<LabelingType<T>> labeling = interactiveLabeling.model().labeling();
		Converter<LabelingType<T>, IntType> converter = (i, o) -> {
			if(i.size() == 0) {
				o.setZero();
				return;
			}
			List<T> sortedLabels = new ArrayList<>(i);
			sortedLabels.sort(interactiveLabeling.model().getLabelComparator());
			try {
				o.set((int) sortedLabels.get(0).getRealFloat());
			} catch(ClassCastException e) {
				o.set((int) (Object)sortedLabels.get(0));
			}
		};
		return Converters.convert(labeling, converter, new IntType());
	}

	public void showRenderer(LabelEditorRenderer renderer) {
		//TODO replace this as soon as SCIFIO can display ARGB
		if(ui != null) {
			ui.show(renderer.getOutput());
		}
//		ImageJFunctions.show(renderer.getOutput());
	}

	public void showData() {
		show(interactiveLabeling.model().getData());
	}

	private void show(RandomAccessibleInterval img) {
		if(ui != null) {
			ui.show(img);
//		} else {
//			ImageJFunctions.show(img);
		}
	}

	public void showTables() {
		InteractiveTableDisplayViewer viewer = new InteractiveTableDisplayViewer(new LabelEditorTable(interactiveLabeling));
		viewer.display();
//		ui.show(new LabelEditorTable(model));
	}
}
