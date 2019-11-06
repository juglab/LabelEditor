package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBvvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
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
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class E04_Open3DLabelingBVV {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel<Integer> panel;

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
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(img, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		//add to BVV
		ImgPlus imgPlus = ij.op().create().imgPlus(ij.op().create().img(imgArgb));
		panel = new LabelEditorBvvPanel<>();
		panel.init(imgPlus, labeling);
		for (LabelingType<Integer> labels : labeling) {
			for (Integer label : labels) {
				panel.model().tagging().addTag(label, label);
				panel.rendering().setTagColor(label, ARGBType.rgba(random.nextInt(255), random.nextInt(255), random.nextInt(255), 150));

			}
		}
		panel.rendering().setTagColor(LabelEditorTag.MOUSE_OVER, ARGBType.rgba(255,255,255,255));
		panel.updateLabelRendering();
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	@AfterClass
	public static void dispose() {
		ij.context().dispose();
		frame.dispose();
		panel.dispose();
	}


	public static void main(String... args) throws IOException {
		new E04_Open3DLabelingBVV().run();
	}


}
