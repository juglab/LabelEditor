/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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

import bdv.cache.CacheControl;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.util.MipmapTransforms;
import bdv.viewer.Interpolation;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerState;
import bdv.viewer.render.DefaultMipmapOrdering;
import bdv.viewer.render.MipmapOrdering;
import bdv.viewer.render.MipmapOrdering.Level;
import bdv.viewer.render.MipmapOrdering.MipmapHints;
import bdv.viewer.render.Prefetcher;
import bdv.viewer.render.VolatileHierarchyProjector;
import bdv.viewer.render.VolatileProjector;
import net.imglib2.Dimensions;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.Volatile;
import net.imglib2.cache.iotiming.CacheIoTiming;
import net.imglib2.cache.volatiles.CacheHints;
import net.imglib2.cache.volatiles.LoadingStrategy;
import net.imglib2.converter.Converter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.SimpleInterruptibleProjector;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

class BdvLabelingRenderer
{

	/**
	 * Thread that triggers repainting of the display.
	 * Requests for repainting are send there.
	 */
	private final PainterThread painterThread;

	/**
	 * Double-buffer index of next {@link #screenImages image} to render.
	 */
	private final ArrayDeque< Integer > renderIdQueue;

	/**
	 * Currently active projector, used to re-paint the display. It maps the
	 * source data to {@link #screenImages}.
	 */
	private VolatileProjector projector;

	/**
	 * Whether double buffering is used.
	 */
	private final boolean doubleBuffered;

	/**
	 * Storage for mask images of {@link VolatileHierarchyProjector}.
	 * One array per visible source. (First) index is index in list of visible sources.
	 */
	private byte[][] renderMaskArrays;

	/**
	 * Used to render the image for display. Three images per screen resolution
	 * if double buffering is enabled. First index is double-buffer.
	 */
	private IntScreenImage[] screenImages;

	private AffineTransform3D screenScaleTransform;

	/**
	 * Whether the current rendering operation may be cancelled (to start a
	 * new one). Rendering may be cancelled unless we are rendering at
	 * coarsest screen scale and coarsest mipmap level.
	 */
	private volatile boolean renderingMayBeCancelled;

	/**
	 * How many threads to use for rendering.
	 */
	private final int numRenderingThreads;

	/**
	 * {@link ExecutorService} used for rendering.
	 */
	private final ExecutorService renderingExecutorService;

	/**
	 * Controls IO budgeting and fetcher queue.
	 */
	private final CacheControl cacheControl;

	/**
	 * Whether volatile versions of sources should be used if available.
	 */
	private final boolean useVolatileIfAvailable;

	/**
	 * Whether a repaint was {@link #requestRepaint() requested}. This will
	 * cause {@link CacheControl#prepareNextFrame()}.
	 */
	private boolean newFrameRequest;

	private int previousTimepoint;

	// TODO: should be settable
	private long[] iobudget = new long[] { 100l * 1000000l,  10l * 1000000l };

	// TODO: should be settable
	private boolean prefetchCells = true;

	/**
	 * @param painterThread
	 *            Thread that triggers repainting of the display. Requests for
	 *            repainting are send there.
	 * @param doubleBuffered
	 *            Whether to use double buffered rendering.
	 * @param numRenderingThreads
	 *            How many threads to use for rendering.
	 * @param renderingExecutorService
	 *            if non-null, this is used for rendering. Note, that it is
	 *            still important to supply the numRenderingThreads parameter,
	 *            because that is used to determine into how many sub-tasks
	 *            rendering is split.
	 * @param useVolatileIfAvailable
	 *            whether volatile versions of sources should be used if
	 *            available.
	 * @param cacheControl
	 *            the cache controls IO budgeting and fetcher queue.
	 */
	BdvLabelingRenderer(
			final PainterThread painterThread,
			final boolean doubleBuffered,
			final int numRenderingThreads,
			final ExecutorService renderingExecutorService,
			final boolean useVolatileIfAvailable,
			final CacheControl cacheControl)
	{
		this.painterThread = painterThread;
		projector = null;
		renderIdQueue = new ArrayDeque<>();
		this.doubleBuffered = doubleBuffered;
		renderMaskArrays = new byte[ 0 ][];
		screenImages = new IntScreenImage[ 3 ];
		screenScaleTransform = new AffineTransform3D();

		renderingMayBeCancelled = true;
		this.numRenderingThreads = numRenderingThreads;
		this.renderingExecutorService = renderingExecutorService;
		this.useVolatileIfAvailable = useVolatileIfAvailable;
		this.cacheControl = cacheControl;
		newFrameRequest = false;
		previousTimepoint = -1;
	}

	/**
	 * Check whether the size of the display component was changed and
	 * recreate {@link #screenImages} and {@link #screenScaleTransform} accordingly.
	 *
	 * @return whether the size was changed.
	 */
	private synchronized boolean checkResize(int width, int height)
	{
		if ( screenImages[ 0 ] == null || screenImages[ 0 ].dimension( 0 ) != width || screenImages[ 0 ].dimension( 1 ) != height)
		{
			renderIdQueue.clear();
			renderIdQueue.addAll( Arrays.asList( 0, 1, 2 ) );
			final double screenToViewerScale = 1;
			final int w = ( int ) ( screenToViewerScale * width);
			final int h = ( int ) ( screenToViewerScale * height);
			if ( doubleBuffered )
			{
				for ( int b = 0; b < 3; ++b )
				{
					// reuse storage arrays of level 0 (highest resolution)
					screenImages[ b ] = new IntScreenImage( w, h );
				}
			}
			else
			{
				screenImages[ 0 ] = new IntScreenImage( w, h );
			}
			final AffineTransform3D scale = new AffineTransform3D();
			final double xScale = ( double ) w / width;
			final double yScale = ( double ) h / height;
			scale.set( xScale, 0, 0 );
			scale.set( yScale, 1, 1 );
			scale.set( 0.5 * xScale - 0.5, 0, 3 );
			scale.set( 0.5 * yScale - 0.5, 1, 3 );
			screenScaleTransform = scale;

			return true;
		}
		return false;
	}

	private void checkRenewMaskArrays()
	{
		if ( 1 != renderMaskArrays.length ||
				((renderMaskArrays[0].length < screenImages[0].size())) )
		{
			final int size = ( int ) screenImages[ 0 ].size();
			renderMaskArrays = new byte[1][];
			for (int j = 0; j < 1; ++j )
				renderMaskArrays[ j ] = new byte[ size ];
		}
	}

	private final AffineTransform3D currentProjectorTransform = new AffineTransform3D();

	void paint(final int width, final int height, final ViewerState state, SourceAndConverter<?> sourceState)
	{

		final boolean resized = checkResize(width, height);

		// the projector that paints to the screenImage.
		final VolatileProjector p;

		final boolean clearQueue;

		final boolean createProjector;

		synchronized ( this )
		{
			clearQueue = newFrameRequest;
			if ( clearQueue )
				cacheControl.prepareNextFrame();
			createProjector = newFrameRequest || resized;
			newFrameRequest = false;

			if ( createProjector )
			{
				final int renderId = renderIdQueue.peek();
				final IntScreenImage screenImage = screenImages[ renderId ];
				synchronized ( state )
				{
					checkRenewMaskArrays();
					p = createProjector( state, sourceState, screenImage );
				}
				projector = p;
			}
			else
			{
				p = projector;
			}
		}

		// try rendering
		p.map(createProjector);
	}

	/**
	 * Request a repaint of the display from the painter thread, with maximum
	 * screen scale index and mipmap level.
	 */
	synchronized void requestRepaint()
	{
		newFrameRequest = true;
		if ( renderingMayBeCancelled && projector != null )
			projector.cancel();
		painterThread.requestRepaint();
	}

	private VolatileProjector createProjector(
			final ViewerState viewerState,
			SourceAndConverter sourceState, final IntScreenImage screenImage)
	{
		VolatileProjector projector = createSingleSourceProjector( viewerState, sourceState, screenImage, renderMaskArrays[ 0 ] );

		previousTimepoint = viewerState.getCurrentTimepoint();
		viewerState.getViewerTransform( currentProjectorTransform );
		CacheIoTiming.getIoTimeBudget().reset( iobudget );
		return projector;
	}

	public IntScreenImage getScreenImage() {
		return screenImages[0];
	}

	private static class SimpleVolatileProjector< A, B > extends SimpleInterruptibleProjector< A, B > implements VolatileProjector
	{
		private boolean valid = false;

		public SimpleVolatileProjector(
				final RandomAccessible< A > source,
				final Converter< ? super A, B > converter,
				final RandomAccessibleInterval< B > target,
				final int numThreads,
				final ExecutorService executorService )
		{
			super( source, converter, target, numThreads, executorService );
		}

		@Override
		public boolean map( final boolean clearUntouchedTargetPixels )
		{
			final boolean success = super.map();
			valid |= success;
			return success;
		}

		@Override
		public boolean isValid()
		{
			return valid;
		}
	}

	private < T extends IntegerType<?>> VolatileProjector createSingleSourceProjector(
			final ViewerState viewerState,
			final SourceAndConverter< T > source,
			final IntScreenImage screenImage,
			final byte[] maskArray )
	{
		if ( useVolatileIfAvailable )
		{
			if ( source.asVolatile() != null )
				return createSingleSourceVolatileProjector( viewerState, source.asVolatile().getSpimSource(), screenImage, maskArray );
			else if ( source.getSpimSource().getType() instanceof Volatile )
			{
				@SuppressWarnings( "unchecked" )
				final SourceAndConverter< ? extends Volatile > vsource = ( SourceAndConverter< ? extends Volatile< ? > > ) source;
				return createSingleSourceVolatileProjector( viewerState, vsource.getSpimSource(), screenImage, maskArray );
			}
		}

		final AffineTransform3D screenScaleTransform = this.screenScaleTransform;
		final int bestLevel = getBestMipMapLevel( viewerState, source, screenScaleTransform );
		Converter<? super T, IntType> converter = (Converter<T, IntType>) (input, output) -> output.set(input.getInteger());
		return new SimpleVolatileProjector<>(
				getTransformedSource( viewerState, source.getSpimSource(), screenScaleTransform, bestLevel, null ),
				converter, screenImage, numRenderingThreads, renderingExecutorService );
	}

	private static int getBestMipMapLevel(
			final ViewerState viewerState,
			final SourceAndConverter< ? > source,
			final AffineTransform3D screenTransform )
	{
		return MipmapTransforms.getBestMipMapLevel( screenTransform, source.getSpimSource(), viewerState.getCurrentTimepoint() );
	}

	private < T extends Volatile< ? extends IntegerType<?> > > VolatileProjector createSingleSourceVolatileProjector(
			final ViewerState viewerState,
			final Source< T > source,
			final IntScreenImage screenImage,
			final byte[] maskArray )
	{
		final AffineTransform3D screenScaleTransform = this.screenScaleTransform;
		final ArrayList< RandomAccessible< T > > renderList = new ArrayList<>();
		final int t = viewerState.getCurrentTimepoint();

		final MipmapOrdering ordering = source instanceof MipmapOrdering ?
			( MipmapOrdering ) source : new DefaultMipmapOrdering(source);

		final AffineTransform3D screenTransform = new AffineTransform3D();
		viewerState.getViewerTransform( screenTransform );
		screenTransform.preConcatenate( screenScaleTransform );
		final MipmapHints hints = ordering.getMipmapHints( screenTransform, t, previousTimepoint );
		final List< Level > levels = hints.getLevels();

		if ( prefetchCells )
		{
			levels.sort(MipmapOrdering.prefetchOrderComparator);
			for ( final Level l : levels )
			{
				final CacheHints cacheHints = l.getPrefetchCacheHints();
				if ( cacheHints == null || cacheHints.getLoadingStrategy() != LoadingStrategy.DONTLOAD )
					prefetch( viewerState, source, screenScaleTransform, l.getMipmapLevel(), cacheHints, screenImage );
			}
		}

		levels.sort(MipmapOrdering.renderOrderComparator);
		for ( final Level l : levels )
			renderList.add( getTransformedSource( viewerState, source, screenScaleTransform, l.getMipmapLevel(), l.getRenderCacheHints() ) );

		if ( hints.renewHintsAfterPaintingOnce() )
			newFrameRequest = true;

		Converter<? super T, IntType> converter = (Converter<T, IntType>) (input, output) -> output.set(input.get().getInteger());
		return new VolatileHierarchyProjector<>( renderList, converter, screenImage, maskArray, numRenderingThreads, renderingExecutorService );
	}

	private static < T > RandomAccessible< T > getTransformedSource(
			final ViewerState viewerState,
			final Source< T > source,
			final AffineTransform3D screenScaleTransform,
			final int mipmapIndex,
			final CacheHints cacheHints )
	{
		final int timepoint = viewerState.getCurrentTimepoint();

		final RandomAccessibleInterval< T > img = source.getSource( timepoint, mipmapIndex );
		if (img instanceof VolatileCachedCellImg)
			( ( VolatileCachedCellImg< ?, ? > ) img ).setCacheHints( cacheHints );

		final Interpolation interpolation = viewerState.getInterpolation();
		final RealRandomAccessible< T > ipimg = source.getInterpolatedSource( timepoint, mipmapIndex, interpolation );

		final AffineTransform3D sourceToScreen = new AffineTransform3D();
		viewerState.getViewerTransform( sourceToScreen );
		final AffineTransform3D sourceTransform = new AffineTransform3D();
		source.getSourceTransform( timepoint, mipmapIndex, sourceTransform );
		sourceToScreen.concatenate( sourceTransform );
		sourceToScreen.preConcatenate( screenScaleTransform );

		return RealViews.affine( ipimg, sourceToScreen );
	}

	private static < T > void prefetch(
			final ViewerState viewerState,
			final Source< T > source,
			final AffineTransform3D screenScaleTransform,
			final int mipmapIndex,
			final CacheHints prefetchCacheHints,
			final Dimensions screenInterval )
	{
		final int timepoint = viewerState.getCurrentTimepoint();
		final RandomAccessibleInterval< T > img = source.getSource( timepoint, mipmapIndex );
		if (img instanceof VolatileCachedCellImg)
		{
			final VolatileCachedCellImg< ?, ? > cellImg = ( VolatileCachedCellImg< ?, ? > ) img;

			CacheHints hints = prefetchCacheHints;
			if ( hints == null )
			{
				final CacheHints d = cellImg.getDefaultCacheHints();
				hints = new CacheHints( LoadingStrategy.VOLATILE, d.getQueuePriority(), false );
			}
			cellImg.setCacheHints( hints );
			final int[] cellDimensions = new int[ 3 ];
			cellImg.getCellGrid().cellDimensions( cellDimensions );
			final long[] dimensions = new long[ 3 ];
			cellImg.dimensions( dimensions );
			final RandomAccess< ? > cellsRandomAccess = cellImg.getCells().randomAccess();

			final Interpolation interpolation = viewerState.getInterpolation();

			final AffineTransform3D sourceToScreen = new AffineTransform3D();
			viewerState.getViewerTransform( sourceToScreen );
			final AffineTransform3D sourceTransform = new AffineTransform3D();
			source.getSourceTransform( timepoint, mipmapIndex, sourceTransform );
			sourceToScreen.concatenate( sourceTransform );
			sourceToScreen.preConcatenate( screenScaleTransform );

			Prefetcher.fetchCells( sourceToScreen, cellDimensions, dimensions, screenInterval, interpolation, cellsRandomAccess );
		}
	}
}
