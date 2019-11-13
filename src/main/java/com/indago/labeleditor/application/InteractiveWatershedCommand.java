package com.indago.labeleditor.application;

import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.plugin.behaviours.ModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.modification.SplitSelectedLabels;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.InteractiveCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.widget.Button;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;

import java.util.ArrayList;
import java.util.List;

@Plugin(type= InteractiveCommand.class, name="Interactive Watershed Labeling Splitter")
public class InteractiveWatershedCommand<L> extends InteractiveCommand {

	@Parameter
	ImgPlus data;

	@Parameter
	private ImgLabeling<L, IntType> labeling;

	@Parameter(required = false)
	private LabelEditorModel<L> displayedModel;

	@Parameter(style = NumberWidget.SLIDER_STYLE, min = "0", max = "10", stepSize = "1")
	private double sigma = 0;

	@Parameter(description = "Submit")
	private Button submit;

	@Parameter
	private OpService ops;

	@Override
	public void run() {
		System.out.println("running command");
		if(displayedModel == null && labeling != null) {
			DefaultLabelEditorModel<L> model = new DefaultLabelEditorModel<>(ops.copy().imgLabeling(labeling));
			model.setData(data);
			setInput("displayedModel", model);
		}
		else {
			ops.copy().imgLabeling(displayedModel.labels(), labeling);
			L onlyLabel = displayedModel.labels().getMapping().getLabels().iterator().next();
			SplitSelectedLabels.split(onlyLabel, displayedModel.labels(), data, sigma, ops);
		}
	}

	public static void main(String...args) {
		ImageJ ij = new ImageJ();
		ij.launch();
		List<PluginInfo<InputWidget>> inputWidgets = ij.context().getPluginIndex().getPlugins(InputWidget.class);
		System.out.println(inputWidgets);

		Img data = ij.op().create().img(new long[]{300, 300});

		drawSphere(data, new long[]{170, 170}, 15);
		drawSphere(data, new long[]{170, 215}, 15);

		ij.op().filter().gauss(data, data, 10);

		Img threshold = (Img) ij.op().threshold().otsu(data);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(threshold, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		ij.command().run(InteractiveWatershedCommand.class, true, "labeling", labeling, "data", data);
	}

	private static void drawSphere(Img<DoubleType> img, long[] position, int radius) {
		RandomAccess<DoubleType> ra = img.randomAccess();
		ra.setPosition(position);
		new HyperSphere<>(img, ra, radius).forEach(value -> value.set(25));
	}
}
