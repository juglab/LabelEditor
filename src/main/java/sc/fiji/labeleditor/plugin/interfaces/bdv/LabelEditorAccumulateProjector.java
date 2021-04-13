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
import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import java.util.List;
import java.util.concurrent.ExecutorService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

public class LabelEditorAccumulateProjector extends AccumulateProjector< ARGBType, ARGBType >
{
	public static AccumulateProjectorFactory<ARGBType> factory = new AccumulateProjectorFactory<ARGBType>() {
		@Override
		public VolatileProjector createProjector(
				final List< VolatileProjector > sourceProjectors,
				final List< SourceAndConverter< ? > > sources,
				final List< ? extends RandomAccessible< ? extends ARGBType > > sourceScreenImages,
				final RandomAccessibleInterval< ARGBType > targetScreenImage,
				final int numThreads,
				final ExecutorService executorService )
		{
			return new LabelEditorAccumulateProjector( sourceProjectors, sourceScreenImages, targetScreenImage, numThreads, executorService );
		}
	};

	public LabelEditorAccumulateProjector(
			final List< VolatileProjector > sourceProjectors,
			final List< ? extends RandomAccessible< ? extends ARGBType > > sources,
			final RandomAccessibleInterval< ARGBType > target,
			final int numThreads,
			final ExecutorService executorService )
	{
		super( sourceProjectors, sources, target, numThreads, executorService );
	}

	@Override
	protected void accumulate( final Cursor< ? extends ARGBType >[] accesses, final ARGBType target )
	{
		float alpha = 0, red = 0, green = 0, blue = 0;
		for (int i = accesses.length-1; i >= 0; i--) {
			Cursor<? extends ARGBType> access = accesses[i];
			final int value = access.get().get();
			final float newalpha = ((float) ARGBType.alpha(value)) / 255.f;
			final float newred = ARGBType.red(value);
			final float newgreen = ARGBType.green(value);
			final float newblue = ARGBType.blue(value);
			if(alpha < 0.0001 && newalpha < 0.0001) continue;
			red = (red * alpha + newred * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			green = (green * alpha + newgreen * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			blue = (blue * alpha + newblue * newalpha * (1 - alpha)) / (alpha + newalpha * (1 - alpha));
			alpha = alpha + newalpha * (1 - alpha);
		}
		target.set( ARGBType.rgba( red, green, blue, (int)(alpha*255) ) );
	}
}
