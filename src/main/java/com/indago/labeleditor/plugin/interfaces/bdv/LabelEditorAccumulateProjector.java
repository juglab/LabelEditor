package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.viewer.render.AccumulateProjector;
import bdv.viewer.render.AccumulateProjectorFactory;
import bdv.viewer.render.VolatileProjector;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class LabelEditorAccumulateProjector extends AccumulateProjector< ARGBType, ARGBType >
{
	public static AccumulateProjectorFactory< ARGBType > factory = (
			sourceProjectors,
			sources,
			sourceScreenImages,
			targetScreenImages,
			numThreads,
			executorService) ->
			new LabelEditorAccumulateProjector(
					sourceProjectors,
					sourceScreenImages,
					targetScreenImages,
					numThreads,
					executorService );

	public LabelEditorAccumulateProjector(
			final ArrayList< VolatileProjector > sourceProjectors,
			final ArrayList< ? extends RandomAccessible< ? extends ARGBType > > sources,
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
			final float newalpha = (float) ARGBType.alpha(value) / 255.f;
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
