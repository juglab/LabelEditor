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
//package sc.fiji.labeleditor.plugin.interfaces.bdv;
//
//import net.imagej.ImgPlus;
//import net.imagej.ops.OpService;
//import net.imglib2.RandomAccessibleInterval;
//import net.imglib2.img.array.ArrayImgFactory;
//import net.imglib2.type.numeric.RealType;
//import net.imglib2.type.numeric.integer.IntType;
//import net.imglib2.util.Pair;
//import net.imglib2.view.Views;
//import org.scijava.Priority;
//import org.scijava.plugin.Parameter;
//import org.scijava.plugin.Plugin;
//import org.scijava.ui.swing.widget.SwingInputWidget;
//import org.scijava.widget.InputWidget;
//import org.scijava.widget.WidgetModel;
//import sc.fiji.labeleditor.core.model.LabelEditorModel;
//
//@Plugin(type = InputWidget.class, priority = Priority.HIGH)
//public class SwingLabelEditorModelBdvWidget extends SwingInputWidget<LabelEditorModel> {
//
//	@Parameter
//	OpService ops;
//
//	private LabelEditorBdvPanel<Object> panel;
//	private WidgetModel model;
//	private boolean labelingLoaded = false;
//
//	@Override
//	protected void doRefresh() {
//		if(model.getValue() == null) return;
//		if(!labelingLoaded) {
//			labelingLoaded = true;
//			LabelEditorModel value = (LabelEditorModel) model.getValue();
//			panel.add(value);
//			RandomAccessibleInterval data = panel.model().getData();
//			if(data != null) {
//				Pair minmax = ops.stats().minMax(Views.iterable(data));
//				RealType min = (RealType) minmax.getA();
//				RealType max = (RealType) minmax.getB();
//				panel.getSources().forEach(source -> source.setDisplayRange(0, 255));
//				panel.getSources().get(0).setDisplayRange(min.getRealDouble(), max.getRealDouble());
//			}
//			panel.model().colors().getDefaultFaceColor().set(255,0,0);
//		}
//		else panel.view().updateOnLabelingChange();
//	}
//
//	@Override
//	public LabelEditorModel getValue() {
//		if(panel == null) return null;
//		return panel.model();
//	}
//
//	@Override
//	public void set(final WidgetModel model) {
//		super.set(model);
//		this.model = model;
//		panel = new LabelEditorBdvPanel<>();
//		panel.add(new ImgPlus(new ArrayImgFactory<>(new IntType()).create(200, 100)));
//		getComponent().add(panel.get(), "w 400, h 300, span, grow, push");
//		refreshWidget();
//	}
//
//	@Override
//	public boolean supports(WidgetModel model) {
//		return model.isType(LabelEditorModel.class);
//	}
//}
