package com.indago.labeleditor.plugin.interfaces.bdv;

import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Pair;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

@Plugin(type = InputWidget.class, priority = Priority.HIGH)
public class SwingLabelEditorModelBdvWidget extends SwingInputWidget<LabelEditorModel> {

	@Parameter
	OpService ops;

	private LabelEditorBdvPanel<Object> panel;
	private WidgetModel model;
	private boolean labelingLoaded = false;

	@Override
	protected void doRefresh() {
		if(model.getValue() == null) return;
		if(!labelingLoaded) {
			labelingLoaded = true;
			LabelEditorModel value = (LabelEditorModel) model.getValue();
			panel.init(value);
			Img data = panel.model().getData();
			if(data != null) {
				Pair minmax = ops.stats().minMax(data);
				RealType min = (RealType) minmax.getA();
				RealType max = (RealType) minmax.getB();
				panel.getSources().forEach(source -> source.setDisplayRange(0, 255));
				panel.getSources().get(0).setDisplayRange(min.getRealDouble(), max.getRealDouble());
			}
			panel.model().colors().get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255,0,0,255));
		}
		else panel.view().updateOnLabelingChange();
	}

	@Override
	public LabelEditorModel getValue() {
		if(panel == null) return null;
		return panel.model();
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		this.model = model;
		panel = new LabelEditorBdvPanel<>();
		panel.init(new ImgPlus(new ArrayImgFactory<>(new IntType()).create(200, 100)));
		getComponent().add(panel.get(), "w 400, h 300, span, grow, push");
		refreshWidget();
	}

	@Override
	public boolean supports(WidgetModel model) {
		return model.isType(LabelEditorModel.class);
	}
}
