package com.indago.labeleditor.howto;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
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
import java.util.Random;

public class E10_ChangingInput {

	public void run() throws InterruptedException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(100, 100);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		ImgPlus<IntType> imgPlus = new ImgPlus<>(img);
		for (int i = 0; i < 13; i++) {
			drawRandomSphere(imgPlus, ra, random);
		}
		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
		ij.context().inject(panel);
		panel.init(imgPlus);
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
		for (int i = 0; i < 1300; i++) {
			drawRandomSphere(imgPlus, ra, random);
			ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(imgPlus, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
			panel.init(labeling, imgPlus);
			panel.getSources().forEach(source -> source.setDisplayRange(0, 100));
			Thread.sleep(3000);
		}
	}

	private void drawRandomSphere(Img<IntType> img, RandomAccess<IntType> ra, Random random) {
		ra.setPosition(new int[]{random.nextInt(100), random.nextInt(100)});
		HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
		for (IntType value : hyperSphere)
			try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
	}

	public static void main(String... args) throws InterruptedException {
		new E10_ChangingInput().run();
	}
}
