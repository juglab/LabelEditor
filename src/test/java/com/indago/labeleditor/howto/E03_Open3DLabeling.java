package com.indago.labeleditor.howto;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

public class E03_Open3DLabeling {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<IntType> panel;

	@Test
	@Ignore
	public void run() {
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(100, 100, 100);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		for (int i = 0; i < 13; i++) {
			ra.setPosition(new int[]{random.nextInt(100), random.nextInt(100), random.nextInt(100)});
			HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
			for (IntType value : hyperSphere)
				try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
		}
		ImgLabeling<IntType, IntType> labeling = ij.op().labeling().cca(img, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
		ImgPlus<IntType> imgPlus = new ImgPlus<>(img, "", new AxisType[]{Axes.X, Axes.Y, Axes.Z});
		panel = new LabelEditorBdvPanel<>();
		panel.init(labeling, imgPlus);
		panel.getSources().forEach(source -> source.setDisplayRange(0, 100));
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
		new E03_Open3DLabeling().run();
	}


}
