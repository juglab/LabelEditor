package sc.fiji.labeleditor.application.interactive;

import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import org.scijava.command.CommandModule;
import org.scijava.plugin.PluginInfo;
import org.scijava.widget.InputWidget;
import sc.fiji.labeleditor.application.InteractiveWatershedCommand;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class RunInteractiveWatershedCommand {

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
