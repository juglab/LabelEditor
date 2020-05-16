package sc.fiji.labeleditor.application;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.view.Views;
import org.scijava.Cancelable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.NumberWidget;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.behaviours.modification.SplitLabels;

import java.util.Random;

@Plugin(type= Command.class, name="Interactive Watershed Labeling Splitter")
public class InteractiveWatershedCommand<L> implements Command, Cancelable {

	@Parameter
	RandomAccessibleInterval data;

	@Parameter
	private ImgLabeling<L, ? extends IntegerType<?> > labeling;

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
			ops.copy().imgLabeling(output.labeling(), (ImgLabeling) labeling);
			if(backgroundDarker) {
				ops.image().invert(Views.iterable((RandomAccessibleInterval) output.getData()),
						Views.iterable(data));
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

}
