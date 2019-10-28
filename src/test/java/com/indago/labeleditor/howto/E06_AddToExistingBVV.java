package com.indago.labeleditor.howto;

import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.BvvActionHandler;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

public class E06_AddToExistingBVV {

	@Test
	public void run() {

		//create img with spheres at random places
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(100, 100, 100);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		for (int i = 0; i < 13; i++) {
			ra.setPosition(new int[]{random.nextInt(100), random.nextInt(100), random.nextInt(100)});
			HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
			for (IntType value : hyperSphere)
				try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
		}

		//convert img to color
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(ARGBType.rgba(i.get(), i.get(), i.get(), 155));
		RandomAccessibleInterval<ARGBType> imgArgb = Converters.convert((RandomAccessibleInterval<IntType>) img, converter, new ARGBType());

		//compute cca
		ImageJ ij = new ImageJ();
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(img, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		//create model and renderer
		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		LabelEditorRenderer<Integer> renderer = new DefaultLabelEditorRenderer<>(model);
		for (LabelingType<Integer> labels : labeling) {
			for (Integer label : labels) {
				model.addTag(label, label);
				renderer.setTagColor(label, ARGBType.rgba(random.nextInt(255), random.nextInt(255), random.nextInt(255), 150));

			}
		}
		//add to BVV
		BvvStackSource<ARGBType> source1 = BvvFunctions.show(imgArgb, "RAW", Bvv.options());
		BvvFunctions.show(renderer.getRenderedLabels(), "labels", Bvv.options().addTo(source1));

		ActionHandler<Integer> actionHandler = new BvvActionHandler<>(source1.getBvvHandle(), model, renderer);
		actionHandler.init();
	}

	public static void main(String... args) throws IOException {
		new E06_AddToExistingBVV().run();
	}


}
