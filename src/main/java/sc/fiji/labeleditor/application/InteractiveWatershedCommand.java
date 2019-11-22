package sc.fiji.labeleditor.application;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorTargetComponent;
import sc.fiji.labeleditor.plugin.behaviours.modification.SplitLabels;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginInfo;
import org.scijava.ui.UIService;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

@Plugin(type= Command.class, name="Interactive Watershed Labeling Splitter")
public class InteractiveWatershedCommand<L> implements Command, Cancelable {

	@Parameter
	RandomAccessibleInterval data;

	@Parameter
	private ImgLabeling<L, IntType> labeling;

	@Parameter(required = false, type = ItemIO.BOTH)
	private LabelEditorModel<L> output;

	@Parameter(style = NumberWidget.SLIDER_STYLE, min = "1", max = "10", stepSize = "1", callback = "update")
	private double sigma = 0;

	@Parameter(description = "Dark BG?")
	private boolean backgroundDarker = true;

	@Parameter
	private OpService ops;

	@Parameter
	private UIService ui;

	private boolean canceled = false;

	private void update() {
		if(output == null && labeling != null) {
			LabelEditorModel<L> model = new DefaultLabelEditorModel<>(ops.copy().imgLabeling(labeling), ops.copy().rai(data));
//			setInput("displayedModel", model);
			output = model;
		}
		else {
			ops.copy().imgLabeling(output.labeling(), labeling);
			if(backgroundDarker) {
				ops.image().invert(Views.iterable(output.getData()), Views.iterable(data));
			} else {
				ops.copy().rai(output.getData(), data);
			}
			L onlyLabel = output.labeling().getMapping().getLabels().iterator().next();
			SplitLabels.split(onlyLabel, output.labeling(), output.getData(), sigma, ops);
			Random random = new Random();
			output.labeling().getMapping().getLabels().forEach(label -> {
				output.tagging().addTagToLabel(label, label);
				output.colors().getFaceColor(label).set(randomColor(random));
			});
		}
	}

	@Override
	public void run() {

	}

	private int randomColor(Random random) {
		return ARGBType.rgba(random.nextInt(155)+100, random.nextInt(155) + 100, random.nextInt(255) + 100, 200);
	}

	@Override
	public boolean isCanceled() {
		return canceled;
	}

	@Override
	public void cancel(String reason) {
		output = null;
	}

	@Override
	public String getCancelReason() {
		return null;
	}

	public static void main(String...args) throws ExecutionException, InterruptedException {
		ImageJ ij = new ImageJ();
		ij.launch();
		List<PluginInfo<InputWidget>> inputWidgets = ij.context().getPluginIndex().getPlugins(InputWidget.class);
		System.out.println(inputWidgets);

		Img<DoubleType> data = ij.op().create().img(new long[]{300, 300});

		drawSphere(data, new long[]{170, 170}, 15);
		drawSphere(data, new long[]{170, 215}, 15);

		ij.op().filter().gauss(data, data, 2);

		Img dataInverted = ij.op().create().img(data);
		ij.op().image().invert(dataInverted, data);

		Img threshold = (Img) ij.op().threshold().otsu(data);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(threshold, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		CommandModule res = ij.command().run(InteractiveWatershedCommand.class, true, "labeling", labeling, "data", dataInverted).get();
		System.out.println(res.getOutput("output"));
	}

	private static void drawSphere(Img<DoubleType> img, long[] position, int radius) {
		RandomAccess<DoubleType> ra = img.randomAccess();
		ra.setPosition(position);
		new HyperSphere<>(img, ra, radius).forEach(value -> value.set(25));
	}
}
