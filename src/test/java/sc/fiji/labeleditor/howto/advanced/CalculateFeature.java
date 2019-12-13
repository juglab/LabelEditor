package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.Cursor;
import net.imglib2.RealLocalizable;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.Masks;
import net.imglib2.roi.Regions;
import net.imglib2.roi.geom.real.Polygon2D;
import net.imglib2.type.logic.BitType;
import net.imglib2.util.Localizables;
import org.junit.Test;

import java.util.Arrays;

public class CalculateFeature {

	@Test
	public void calculateSize() {

		ImageJ ij = new ImageJ();

		//create image, set all pixels to one
		ArrayImg<BitType, ?> image2d = new ArrayImgFactory<>(new BitType()).create(3, 3);
		image2d.forEach(pixel -> pixel.setOne());

		//get contour
		Polygon2D poly = ij.op().geom().contour( image2d, false );

		System.out.println("Contour: ");
		for (RealLocalizable vertex : poly.vertices()) {
			System.out.println(vertex);
		}

		System.out.println("\nPixels of iterable region: ");

		Cursor<BitType> cursor = Regions.sample(Masks.toIterableRegion(poly), image2d).localizingCursor();
		while(cursor.hasNext()) {
			cursor.next();
			System.out.println(Arrays.toString(Localizables.asLongArray(cursor)));
		}

	}

}
