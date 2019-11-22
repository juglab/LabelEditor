package sc.fiji.labeleditor.application;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, name = "Connected Component Analysis")
public class ConnectedComponentAnalysis extends DynamicCommand {

	@Parameter
	Img<BitType> binaryInput;

	@Parameter(type = ItemIO.OUTPUT)
	private ImgLabeling<Integer, IntType> output;

	@Parameter(label = "CCA structuring element", choices = {"four-connected", "eight-connected"}, required = false)
	String structuringElementChoice = "four-connected";

	@Parameter(label = "Process in slices", required = false)
	boolean processInSlices;

	@Parameter
	OpService opService;

	@Override
	public void run() {

		System.out.println(processInSlices);

		ConnectedComponents.StructuringElement structuringElement = ConnectedComponents.StructuringElement.FOUR_CONNECTED;
		if(structuringElementChoice.equals("eight-connected")) {
			structuringElement = ConnectedComponents.StructuringElement.EIGHT_CONNECTED;
		}

		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create(binaryInput);
		ImgLabeling< Integer, IntType > cca = new ImgLabeling<>( backing );
		if(processInSlices && binaryInput.numDimensions() > 2) {
			for (int i = 0; i < binaryInput.dimension(2); i++) {
				IntervalView<BitType> slice = Views.hyperSlice(binaryInput, 2, i);
				ImgLabeling<Integer, IntType> sliceRes = opService.labeling().cca(slice, structuringElement);
				IntervalView<LabelingType<Integer>> ccaSlice = Views.hyperSlice(cca, 2, i);
				Cursor<LabelingType<Integer>> sliceCursor = sliceRes.localizingCursor();
				RandomAccess<LabelingType<Integer>> resRA = ccaSlice.randomAccess();
				while(sliceCursor.hasNext()) {
					LabelingType<Integer> val = sliceCursor.next();
					resRA.setPosition(sliceCursor);
					resRA.get().addAll(val);
				}
			}
		} else {
			opService.labeling().cca(cca, binaryInput, structuringElement);
		}
		output = cca;
	}
}
