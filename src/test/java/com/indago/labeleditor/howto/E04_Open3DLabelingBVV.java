package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBvvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
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
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class E04_Open3DLabelingBVV {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorPanel<Integer> panel;

	@Test
	public <T extends RealType<T>> void run() throws IOException {
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		RandomAccessibleInterval inputStack = input;
		List<RandomAccessibleInterval<T>> stack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			stack.add(inputStack);
		}
		inputStack = Views.stack(stack);
		Img thresholded = (Img) ij.op().threshold().otsu(Views.iterable(inputStack));
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		model.labelRegions().forEach((label, regions) -> {
			model.tagging().addTag("displayed", label);
		});

		panel = new LabelEditorBvvPanel<>();
		panel.init(model);
		panel.rendering().setTagColor("displayed", ARGBType.rgba(255,255,0,55));
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
