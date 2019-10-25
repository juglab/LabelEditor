package com.indago.labeleditor.util;

import net.imglib2.IterableInterval;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.ValuePair;

import java.util.Iterator;

public class ImgLib2Util {

	public static <T extends RealType<T>> ValuePair<T, T> computeMinMax(
			final IterableInterval<T> iterableInterval) {

		final T min = iterableInterval.firstElement().copy();
		final T max = min.copy();

		// create a cursor for the image (the order does not matter)
		final Iterator< T > iterator = iterableInterval.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		min.set( type );
		max.set( type );

		// loop over the rest of the data and determine min and max value
		while ( iterator.hasNext() ) {
			// we need this type more than once
			type = iterator.next();

			if ( type.compareTo( min ) < 0 ) min.set( type );

			if ( type.compareTo( max ) > 0 ) max.set( type );
		}

		return new ValuePair<>(min, max);
	}


}
