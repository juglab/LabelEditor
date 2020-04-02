package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * How to use label editor with ImgLabeling<..., UnsignedByteType>
 */
public class E09_ImLabelingUnsignedByteType {

	public static void main(String... args) {
		ImageJ ij = new ImageJ();
		Img< UnsignedByteType > indexImage = ArrayImgs.unsignedBytes(2, 2);
		ImgLabeling< String, UnsignedByteType > labeling = new ImgLabeling<>(indexImage);
		RandomAccess< LabelingType< String > > ra = labeling.randomAccess();
		ra.setPosition(new long[]{0,0});
		ra.get().add("A");
		ra.setPosition(new long[]{1,1});
		ra.get().add("B");
		ij.ui().show(labeling);
	}
}
