package com.indago.labeleditor.application;

import com.indago.labeleditor.application.workflow.ImageWorkflowCommand;
import com.indago.labeleditor.application.workflow.ImageWorkflowStep;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.plugins.commands.binary.Binarize;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Initializable;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.module.Module;
import org.scijava.module.ModuleException;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Plugin(type = Command.class, name = "Objects Counter (IJ2)")
public class ObjectsCounter<L> extends ImageWorkflowCommand implements Initializable {


	@Parameter(type = ItemIO.OUTPUT)
	LabelEditorModel model;

	@Parameter
	UIService uiService;

	ImageWorkflowStep threshold = new ImageWorkflowStep("Threshold", "This step transforms the image into binary format.");
	ImageWorkflowStep cca = new ImageWorkflowStep("CCA", "Thie step performs Connected Component Analysis to detect objects..");

	private List<ImageWorkflowStep> steps;

	@Override
	public void initialize() {
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

		//threshold
		IterableInterval thresholdInput = Views.iterable(opService.copy().rai(input));
		Dataset binaryDataset = new DefaultDataset(context, new ImgPlus((Img)thresholdInput));
		runCommand(threshold, Binarize.class, "inputData", binaryDataset, "changeInput", true);

		Img<BitType> binaryImg = (Img<BitType>) binaryDataset.getImgPlus().getImg();

		//cca
		Module ccaCommand = runCommand(cca, ConnectedComponentAnalysis.class, "binaryInput", binaryImg);
		ImgLabeling<Integer, IntType> labeling = (ImgLabeling<Integer, IntType>) ccaCommand.getInput("output");

		model = new DefaultLabelEditorModel(labeling);
		model.setData((Img)opService.copy().rai(input));

	}

	@Override
	protected List<ImageWorkflowStep> getSteps() {
		return steps;
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
//		Img input = (Img) ij.io().open(ObjectsCounter.class.getResource("/blobs.png").getPath());
		Img source = (Img) ij.io().open("/home/random/Development/imagej/project/3DAnalysisFIBSegmentation/High_glucose_Cell_1_complete-crop.tif");
		IntervalView input = Views.hyperSlice(source, 2, 1);
		ij.command().run(ObjectsCounter.class, true, "input", input);
	}

}
