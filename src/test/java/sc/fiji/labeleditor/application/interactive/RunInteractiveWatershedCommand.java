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
