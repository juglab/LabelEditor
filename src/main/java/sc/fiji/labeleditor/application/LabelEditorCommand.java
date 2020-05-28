/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.application;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import org.scijava.ItemIO;
import org.scijava.ItemVisibility;
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

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String line1 = "";

	@Parameter(label = "Split by channels")
	private boolean hasChannels = false;

	@Parameter(label = "Channel dimension")
	private int channelDimension;

	@Parameter(label = "Raw channels (comma separated indices)")
	private String rawChannels = "";

	@Parameter(label = "Labeling channels (comma separated indices)")
	private String labelingChannels = "";

//	@Parameter(visibility = ItemVisibility.MESSAGE)
//	private String line2 = "";
//
//	@Parameter(label = "Copy labeling slices")
//	private boolean copyIntoNewImage = true;
//
//	@Parameter(visibility = ItemVisibility.MESSAGE)
//	private String copyNote = "<html><p style='font-weight: normal;'>When editing labels make sure to check the last option or<br>close any other open windows of the label map image to avoid interference.</p></html>";

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
		output = new LabelMap<>(labelRAI, rawList, hasChannels);
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
//				if(copyIntoNewImage) labelingsList.add(opService.copy().rai(slice));
//				else labelingsList.add(slice);
				labelingsList.add(slice);
			}
			return Views.stack(new ArrayList<>(labelingsList));
		} else {
//			if(copyIntoNewImage) return opService.copy().rai(input);
			return input;
		}
	}

	private List<RandomAccessibleInterval<? extends RealType<?>>> makeList(int[] channels) {
		if(hasChannels) {
			List<RandomAccessibleInterval<? extends RealType<?>>> res = new ArrayList<>();
			for (int i : channels) {
				RandomAccessibleInterval<? extends RealType<?>> slice = Views.hyperSlice(input, channelDimension, i);
//				if(copyIntoNewImage) res.add(opService.copy().rai(slice));
//				else res.add(slice);
				res.add(slice);
			}
			return res;
		} else {
			return Collections.emptyList();
		}
	}

}
