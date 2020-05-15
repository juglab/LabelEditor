package sc.fiji.labeleditor.application;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Plugin(type= Command.class, menuPath="Plugins>LabelEditor", name = "LabelEditor")
public class LabelEditorCommand<I extends IntegerType<I>> implements Command {

	@Parameter
	private RandomAccessibleInterval<I> input;

	@Parameter
	private boolean hasChannels = false;

	@Parameter
	private int channelDimension;

	@Parameter(label = "Raw channels (comma separated indices)")
	private String rawChannels = "";

	@Parameter(label = "Labeling channels (comma separated indices)")
	private String labelingChannels = "";

	@Parameter(label = "Create new label image (will not include raw data)")
	private boolean copyIntoNewImage = true;

	@Parameter(type = ItemIO.OUTPUT)
	private LabelMap output;

	@Parameter
	private OpService opService;

	@Override
	public void run() {
		int[] labelings = asIntArray(labelingChannels.trim());
		int[] raws = asIntArray(rawChannels.trim());
		RandomAccessibleInterval<I> labelRAI = makeStack(labelings);
		List<RandomAccessibleInterval<? extends RealType<?>>> rawList = makeList(raws);
		output = new LabelMap(labelRAI, rawList, hasChannels);
	}

	private int[] asIntArray(String channels) {
		if(channels.isEmpty()) return new int[0];
		String[] split = channels.split(",");
		int[] res = new int[split.length];
		if(res.length > 0 && hasChannels) {
			for (int i = 0; i < split.length; i++) {
				res[i] = Integer.parseInt(split[i]);
			}
		}
		return res;
	}

	private RandomAccessibleInterval<I> makeStack(int[] channels) {
		if(hasChannels) {
			List<RandomAccessibleInterval<I>> labelingsList = new ArrayList<>();
			for (int i : channels) {
				RandomAccessibleInterval<I> slice = Views.hyperSlice(input, channelDimension, i);
				if(copyIntoNewImage) labelingsList.add(opService.copy().rai(slice));
				else labelingsList.add(slice);
			}
			return Views.stack(new ArrayList<>(labelingsList));
		} else {
			if(copyIntoNewImage) return opService.copy().rai(input);
			return input;
		}
	}

	private List<RandomAccessibleInterval<? extends RealType<?>>> makeList(int[] channels) {
		if(hasChannels) {
			List<RandomAccessibleInterval<? extends RealType<?>>> res = new ArrayList<>();
			for (int i : channels) {
				RandomAccessibleInterval<? extends RealType<?>> slice = Views.hyperSlice(input, channelDimension, i);
				if(copyIntoNewImage) res.add(opService.copy().rai(slice));
				else res.add(slice);
			}
			return res;
		} else {
			return Collections.emptyList();
		}
	}

}
