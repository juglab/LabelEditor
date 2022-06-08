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
package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.viewer.SourceAndConverter;
import bdv.viewer.render.AccumulateProjectorARGB;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.type.numeric.ARGBType;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.view.LabelEditorOverlayRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class LabelEditorAccumulateProjector extends AccumulateProjectorARGB
{

	private final int indexFirstLabeling;

	static AccumulateProjectorFactory< ARGBType > createFactory(BdvInterface bdvInterface) {
		return new AccumulateProjectorFactory< ARGBType >() {
			@Override
			public VolatileProjector createProjector(
					final List< VolatileProjector > sourceProjectors,
					final List<SourceAndConverter< ? >> sources,
					final List< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
					final RandomAccessibleInterval< ARGBType > targetScreenImage,
					final int numThreads,
					final ExecutorService executorService) {
				return new LabelEditorAccumulateProjector( bdvInterface, sourceProjectors, sources, sourceScreenImages, targetScreenImage, numThreads, executorService);
			}
		};
	}

	private LabelEditorAccumulateProjector(
			final BdvInterface bdvInterface,
			final List<VolatileProjector> sourceProjectors,
			final List<SourceAndConverter<?>> sources,
			final List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
			final RandomAccessibleInterval<ARGBType> target,
			final int numThreads,
			final ExecutorService executorService) {
		super( labelEditorProjectors(bdvInterface, sources, sourceScreenImages, target, sourceProjectors),
				labelEditorScreenImages(bdvInterface, sources, sourceScreenImages, target),
				target, numThreads, executorService );
		indexFirstLabeling = sources.size() - labelEditorSourceIndices(bdvInterface, sources).size();
	}

	private static List<Integer> labelEditorSourceIndices(
			final BdvInterface bdvInterface,
			List<SourceAndConverter<?>> sources) {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < sources.size(); i++) {
			for (Map.Entry<SourceAndConverter, InteractiveLabeling<?>> entry : bdvInterface.getIndexSources().entrySet()) {
				if (entry.getKey().equals(sources.get(i))) {
					indices.add(i);
					break;
				}
			}
		}
		return indices;
	}

	private static List<? extends RandomAccessible<? extends ARGBType>> labelEditorScreenImages(
			BdvInterface bdvInterface,
			List<SourceAndConverter<?>> sources,
			List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
			RandomAccessibleInterval<ARGBType> target) {

		List<RandomAccessible<? extends ARGBType>> res = new ArrayList<>();
		List<RandomAccessible<? extends ARGBType>> renderers = new ArrayList<>();

		List<Integer> indices = labelEditorSourceIndices(bdvInterface, sources);

		for (int i = 0; i < sourceScreenImages.size(); i++) {
			RandomAccessible<? extends ARGBType> screenImage = sourceScreenImages.get(i);
			if(indices.contains(i)) {
				addSource(renderers, target, screenImage, bdvInterface.getIndexSources().get(sources.get(i)));
			}
			else {
				res.add(screenImage);
			}
		}
		res.addAll(renderers);
		return res;
	}

	private synchronized static <L> void addSource(
			List<RandomAccessible<? extends ARGBType>> res,
			RandomAccessibleInterval<ARGBType> target,
			RandomAccessible<? extends ARGBType> screenImage,
			InteractiveLabeling<L> labeling) {

		for (LabelEditorRenderer<L> renderer : labeling.view().renderers()) {
			if(!renderer.isActive()) continue;
			if (LabelEditorOverlayRenderer.class.isAssignableFrom(renderer.getClass())) {
				LabelEditorOverlayRenderer<L> overlayRenderer = (LabelEditorOverlayRenderer<L>) renderer;
				ARGBScreenImage argbScreenImage = (ARGBScreenImage) screenImage;
				overlayRenderer.init(labeling.model(), argbScreenImage);
				overlayRenderer.updateOnTagChange();
				res.add(overlayRenderer.getOutput());
			}
		}

	}

	private static List<VolatileProjector> labelEditorProjectors(
			BdvInterface bdvInterface,
			List<SourceAndConverter<?>> sources,
			List<? extends RandomAccessible<? extends ARGBType>> sourceScreenImages,
			RandomAccessibleInterval<ARGBType> target,
			List<VolatileProjector> sourceProjectors) {

		ArrayList<VolatileProjector> res = new ArrayList<>();
		ArrayList<VolatileProjector> rendererProjectors = new ArrayList<>();

		List<Integer> indices = labelEditorSourceIndices(bdvInterface, sources);

		for (int i = 0; i < sources.size(); i++) {

			if(indices.contains(i)) {
				InteractiveLabeling<?> labeling = bdvInterface.getIndexSources().get(sources.get(i));
				for (LabelEditorRenderer<?> renderer : labeling.view().renderers()) {
					if (LabelEditorOverlayRenderer.class.isAssignableFrom(renderer.getClass())) {
						rendererProjectors.add(sourceProjectors.get(i));
					}
				}
			} else {
				res.add(sourceProjectors.get(i));
			}
		}
		sourceProjectors.addAll(rendererProjectors);
		return sourceProjectors;
	}

	protected int accumulate( final int[] values )
	{
		// accumulate sources additively
		int aSum = 0, rSum = 0, gSum = 0, bSum = 0;
		for (int i = 0; i < indexFirstLabeling; i++) {
			final int value = values[i];
			final int a = ARGBType.alpha(value);
			final int r = ARGBType.red(value);
			final int g = ARGBType.green(value);
			final int b = ARGBType.blue(value);
			aSum += a;
			rSum += r;
			gSum += g;
			bSum += b;
		}
		if ( aSum > 255 )
			aSum = 255;
		if ( rSum > 255 )
			rSum = 255;
		if ( gSum > 255 )
			gSum = 255;
		if ( bSum > 255 )
			bSum = 255;

		float alpha = 0, red = 0, green = 0, blue = 0;

		// accumulate labeleditor tag colors as overlay
		for (int i = values.length-1; i >= indexFirstLabeling; i--) {
			final int value = values[i];
			final float newalpha = ((float) ARGBType.alpha(value)) / 255.f;
			final float newred = ARGBType.red(value);
			final float newgreen = ARGBType.green(value);
			final float newblue = ARGBType.blue(value);
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			if(newgreen + newred + newblue < 0.0001) continue;
			red = (red * alpha + newred * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			green = (green * alpha + newgreen * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			blue = (blue * alpha + newblue * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			alpha = alpha + newalpha * (1 - alpha);
		}

		// concatenate additive sources with overlay tag colors
		final float newalpha = ((float)aSum) / 255.f;
		red = (red * alpha + (float) rSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
		green = (green * alpha + (float) gSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
		blue = (blue * alpha + (float) bSum * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
		alpha = alpha + newalpha * (1 - alpha);

		return ARGBType.rgba( red, green, blue, (int)(alpha*255) );
	}

}
