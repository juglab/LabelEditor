package sc.fiji.labeleditor.plugin.behaviours.export;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExportBehaviours extends Behaviours implements LabelEditorBehaviours {

	protected LabelEditorModel model;

	@Parameter
	UIService ui;

	@Parameter
	Context context;

	public ExportBehaviours() {
		super(new InputTriggerConfig(), "labeleditor-export");
	}

	@Override
	public void init(InteractiveLabeling labeling) {
		this.model = labeling.model();
	}

	@Override
	public void install(Behaviours behaviours, Component panel) {

	}

	public ExportLabels getExportSelectedLabels() {
		ExportLabels exportLabels = new ExportLabels(model);
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

	public void showIndexImg() {
		show(model.labeling().getIndexImg());
	}

	public void showLabelMap() {
		show(getLabelMap());
	}

	public <T extends RealType<T>> RandomAccessibleInterval<IntType> getLabelMap() {
		RandomAccessibleInterval<LabelingType<T>> labeling = model.labeling();
		Converter<LabelingType<T>, IntType> converter = (i, o) -> {
			if(i.size() == 0) {
				o.setZero();
				return;
			}
			List<T> sortedLabels = new ArrayList<>(i);
			sortedLabels.sort(model.getLabelComparator());
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
		//ui.show(renderer.getOutput());
		ImageJFunctions.show(renderer.getOutput());
	}

	public void showData() {
		show(model.getData());
	}

	private void show(RandomAccessibleInterval img) {
		if(ui != null) {
			ui.show(img);
		} else {
			ImageJFunctions.show(img);
		}
	}
}
