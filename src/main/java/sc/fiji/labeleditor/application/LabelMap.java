package sc.fiji.labeleditor.application;

import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;

import java.util.List;

public class LabelMap implements RandomAccessibleInterval {

	private final RandomAccessibleInterval<? extends IntegerType<?>> ref;
	private final boolean hasChannels;
	private final List<RandomAccessibleInterval<? extends RealType<?>>> raws;

	public <I extends IntegerType<I>> LabelMap(RandomAccessibleInterval<I> labels, List<RandomAccessibleInterval<? extends RealType<?>>> raws, boolean hasChannels) {
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
	public long max(int d) {
		return ref.max(d);
	}

	@Override
	public RandomAccess randomAccess() {
		return ref.randomAccess();
	}

	@Override
	public RandomAccess randomAccess(Interval interval) {
		return ref.randomAccess();
	}

	@Override
	public int numDimensions() {
		return ref.numDimensions();
	}

	public boolean hasChannels() {
		return hasChannels;
	}

	public List<RandomAccessibleInterval<? extends RealType<?>>> getRaws() {
		return raws;
	}
}
