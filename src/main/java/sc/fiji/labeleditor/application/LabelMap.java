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

import net.imglib2.Interval;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPositionable;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import java.util.List;

public class LabelMap<I extends IntegerType<I>> implements RandomAccessibleInterval<I> {

	private final RandomAccessibleInterval<I> ref;
	private final boolean hasChannels;
	private final List<RandomAccessibleInterval<? extends RealType<?>>> raws;

	public LabelMap(RandomAccessibleInterval<I> labels, List<RandomAccessibleInterval<? extends RealType<?>>> raws, boolean hasChannels) {
		this.hasChannels = hasChannels;
		this.ref = labels;
		this.raws = raws;
	}

	@Override
	public long min(int d) {
		return ref.min(d);
	}

	@Override
	public void min(long[] min) {
		ref.min(min);
	}

	@Override
	public void min(Positionable min) {
		ref.min(min);
	}

	@Override
	public long max(int d) {
		return ref.max(d);
	}

	@Override
	public void max(long[] max) {
		ref.max(max);
	}

	@Override
	public void max(Positionable max) {
		ref.max(max);
	}

	@Override
	public RandomAccess<I> randomAccess() {
		return ref.randomAccess();
	}

	@Override
	public RandomAccess<I> randomAccess(Interval interval) {
		return ref.randomAccess();
	}

	@Override
	public int numDimensions() {
		return ref.numDimensions();
	}

	@Override
	public void dimensions(long[] dimensions) {
		ref.dimensions(dimensions);
	}

	@Override
	public long dimension( final int d ) {
		return ref.dimension(d);
	}

	public boolean hasChannels() {
		return hasChannels;
	}

	public List<RandomAccessibleInterval<? extends RealType<?>>> getRaws() {
		return raws;
	}

	@Override
	public double realMin(int d) {
		return ref.realMin(d);
	}

	@Override
	public void realMin(double[] min) {
		ref.realMin(min);
	}

	@Override
	public void realMin(RealPositionable min) {
		ref.realMin(min);
	}

	@Override
	public double realMax(int d) {
		return ref.realMax(d);
	}

	@Override
	public void realMax(double[] max) {
		ref.realMax(max);
	}

	@Override
	public void realMax(RealPositionable max) {
		ref.realMax(max);
	}
}
