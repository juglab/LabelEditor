package sc.fiji.labeleditor.application;

import sc.fiji.labeleditor.core.model.LabelEditorModel;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.plugins.commands.binary.Binarize;
import net.imagej.workflow.ImageWorkflowCommand;
import net.imagej.workflow.ImageWorkflowStep;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Initializable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Plugin(type = Command.class, menuPath = "Analyze>Objects Counter (IJ2)")
public class ObjectsCounter extends ImageWorkflowCommand implements Initializable {


	@Parameter(type = ItemIO.OUTPUT)
	LabelEditorModel model;

	@Parameter
	UIService uiService;

	ImageWorkflowStep threshold = new ImageWorkflowStep("Threshold", "<html>First, we transform the image into binary format.<br>Please choose from the following options:<br></html>");
	ImageWorkflowStep cca = new ImageWorkflowStep("CCA", "<html>Connected Component Analysis is used to detect objects.<br>Please choose from the following options:<br></html>");

	private List<ImageWorkflowStep> steps;

	@Override
	public void initialize() {
		if(input.numDimensions() > 3) {
			uiService.showDialog("Cannot process images > 3D for now.", DialogPrompt.MessageType.ERROR_MESSAGE);
		}
		steps = new ArrayList<>();
		steps.add(threshold);
		steps.add(cca);
	}

	@Override
	public String getTitle() {
		return "Object Counter (IJ2)";
	}

	@Override
	public void run(RandomAccessibleInterval input) throws InterruptedException, ExecutionException, ModuleException {

		Img<BitType> binaryImg;
		if(!BitType.class.isAssignableFrom(input.randomAccess().get().getClass())) {
			//threshold
			IterableInterval thresholdInput = Views.iterable(opService.copy().rai(input));
			Dataset binaryDataset = new DefaultDataset(context, new ImgPlus((Img)thresholdInput));
			runCommand(threshold, Binarize.class,
					"inputData", binaryDataset,
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

		model = (LabelEditorModel) ccaCommand.getOutput("output");
		model.setData((Img)opService.copy().rai(input));

	}

	@Override
	protected List<ImageWorkflowStep> getSteps() {
		return steps;
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(ObjectsCounter.class.getResource("/blobs.png").getPath());
		Img source = (Img) ij.io().open("/home/random/Development/imagej/project/3DAnalysisFIBSegmentation/High_glucose_Cell_1_complete-crop.tif");
		IntervalView inputslice = Views.hyperSlice(source, 2, 1);
//		input = (Img) ij.op().threshold().otsu(input);
		ij.command().run(ObjectsCounter.class, true, "input", inputslice);
	}

}
