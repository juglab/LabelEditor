package sc.fiji.labeleditor.application;

import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.workflow.DefaultImageWorkflowCommand;
import net.imagej.workflow.DefaultImageWorkflowStep;
import net.imagej.workflow.ImageWorkflowStep;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.Views;
import org.scijava.Initializable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Plugin(type = Command.class, menuPath = "Analyze>Objects Counter (IJ2)")
public class ObjectsCounter extends DefaultImageWorkflowCommand implements Initializable {

	@Parameter
	protected Img input;

	@Parameter(type = ItemIO.OUTPUT)
	LabelEditorModel model;

	ImageWorkflowStep threshold = new DefaultImageWorkflowStep("Threshold", "<html>First, we transform the image into binary format.<br>Please choose from the following options:<br></html>");
	ImageWorkflowStep cca = new DefaultImageWorkflowStep("CCA", "<html>Connected Component Analysis is used to detect objects.<br>Please choose from the following options:<br></html>");

	private List<ImageWorkflowStep> steps;

	@Override
	public void initialize() {
		steps = new ArrayList<>();
		steps.add(threshold);
		steps.add(cca);
	}

	@Override
	public String getTitle() {
		return "Objects Counter (IJ2)";
	}

	@Override
	public RandomAccessibleInterval getInput() {
		return input;
	}

	@Override
	public void run(RandomAccessibleInterval input) throws InterruptedException, ExecutionException, ModuleException {

		if(input.numDimensions() > 3) {
			cancel("Cannot process images > 3D for now.");
			return;
		}

		Img<BitType> binaryImg;
		if(!BitType.class.isAssignableFrom(input.randomAccess().get().getClass())) {
			//threshold
			IterableInterval thresholdInput = Views.iterable(opService.copy().rai(input));
			Dataset binaryDataset = new DefaultDataset(context, new ImgPlus((Img)thresholdInput));
			runCommand(threshold, Binarize.class,
					"inputData", binaryDataset,
					"inputMask", null,
					"changeInput", true,
					"maskColor", Binarize.WHITE,
					"fillBg", true,
					"fillFg", true);

			binaryImg = (Img<BitType>) binaryDataset.getImgPlus().getImg();
		} else {
			binaryImg = (Img<BitType>) input;
		}

		//cca
		Module ccaCommand = runCommand(cca, ConnectedComponentAnalysis.class, "binaryInput", binaryImg);

		ImgLabeling labeling = (ImgLabeling) ccaCommand.getOutput("output");

		if((Boolean) ccaCommand.getInput("processInSlices")) {
			model = new TimeSliceLabelEditorModel(labeling, input, 2);
		} else {
			model = new DefaultLabelEditorModel(labeling, input);
		}
	}

	@Override
	public List<ImageWorkflowStep> getSteps() {
		return steps;
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(ObjectsCounter.class.getResource("/blobs.png").getPath());
		ij.command().run(ObjectsCounter.class, true, "input", input);
	}

}
