package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class E14_ChangingColors {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<Integer> panel;

	@Test
	@Ignore
	public void run() throws InterruptedException {
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(100, 100);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		ImgPlus<IntType> imgPlus = new ImgPlus<>(img);
		for (int i = 0; i < 33; i++) {
			drawRandomSphere(imgPlus, ra, random);
		}
		panel = new LabelEditorBdvPanel<>();
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(imgPlus, ConnectedComponents.StructuringElement.FOUR_CONNECTED);
		panel.init(imgPlus, labeling);
		panel.getSources().forEach(source -> source.setDisplayRange(0, 255));
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
		panel.model().labels().getMapping().getLabels().forEach(label -> {
			panel.model().tagging().addTag(label, label);
		});
		for (int i = 0; i < 13; i++) {
			panel.model().colors().clear();
			panel.model().labels().getMapping().getLabels().forEach(label -> {
				panel.model().colors().get(label).put(LabelEditorTargetComponent.FACE, randomColor(random));
			});
			Thread.sleep(3000);
		}
	}

	private int randomColor(Random random) {
		return ARGBType.rgba(random.nextInt(155)+100, random.nextInt(155) + 100, random.nextInt(155) + 100, 200);
	}

	private void drawRandomSphere(Img<IntType> img, RandomAccess<IntType> ra, Random random) {
		ra.setPosition(new int[]{random.nextInt(100), random.nextInt(100)});
		HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
		for (IntType value : hyperSphere)
			try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
	}

	@AfterClass
	public static void dispose() {
		frame.dispose();
		panel.dispose();
		ij.context().dispose();
	}

	public static void main(String... args) throws InterruptedException {
		new E14_ChangingColors().run();
	}
}
