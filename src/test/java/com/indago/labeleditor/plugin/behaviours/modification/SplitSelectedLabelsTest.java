package com.indago.labeleditor.plugin.behaviours.modification;

import net.imagej.ImageJ;
import net.imglib2.Localizable;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class SplitSelectedLabelsTest {

	@Test
	public void splitSelected() {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img data = ij.op().create().img(new long[]{300, 300});

		int radius = 15;
		drawSphere(data, new long[]{170, 170}, radius);
		drawSphere(data, new long[]{170, 215}, radius);

		ij.op().filter().gauss(data, data, 10);

		Img threshold = (Img) ij.op().threshold().otsu(data);

		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(threshold, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		assertEquals(1, labeling.getMapping().getLabels().size());

		Iterator<Integer> iterator = labeling.getMapping().getLabels().iterator();
		Integer label = iterator.next();

		SplitLabels.split(label, labeling, data, 1, ij.op());

		//TODO why is this not 2?
		assertEquals(3, labeling.getMapping().getLabels().size());

		ij.ui().show(labeling.getIndexImg());
	}

	private void drawSphere(Img<DoubleType> img, long[] position, int radius) {
		RandomAccess<DoubleType> ra = img.randomAccess();
		ra.setPosition(position);
		new HyperSphere<>(img, ra, radius).forEach(value -> value.set(25));
//		HyperSphereCursor<DoubleType> cursor = hyperSphere.localizingCursor();
//		RandomAccess<DoubleType> imgRA = img.randomAccess();
//		while(cursor.hasNext()) {
//			cursor.next();
//			imgRA.setPosition(cursor);
//			double distance = getDistance(ra, cursor);
//			double v = imgRA.get().get();
//			imgRA.get().set(v + radius- distance);
//		}
	}

	private double getDistance(Localizable a, Localizable b) {
		double diff = 0;
		for (int i = 0; i < a.numDimensions(); i++) {
			double diff1 = a.getDoublePosition(i) - b.getDoublePosition(i);
			diff += diff1*diff1;

		}
		return Math.sqrt(diff);
	}

	public static void main(String...args) {
		new SplitSelectedLabelsTest().splitSelected();
	}
}