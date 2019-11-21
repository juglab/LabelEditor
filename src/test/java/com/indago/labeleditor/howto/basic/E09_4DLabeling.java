package com.indago.labeleditor.howto.basic;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.interfaces.bdv.BdvInterface;
import com.indago.labeleditor.plugin.mode.timeslice.TimeSliceLabelEditorModel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How a 3D labeling looks like in the LabelEditor
 */
public class E09_4DLabeling {

	/**
	 * There is nothing specific one has to do to make the LabelEditor display 3D data,
	 * this example just serves as an example.
	 */
	public void run() {
		ImageJ ij = new ImageJ();
		ij.launch();

		int size = 20;
		//create data
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(size, size, size, size);
		RandomAccess<IntType> ra = img.randomAccess();

		Random random = new Random();

		for (int i = 0; i < 20; i++) {
			System.out.println(i);
			ra.setPosition(new int[]{random.nextInt(size), random.nextInt(size), random.nextInt(size), random.nextInt(size)});
			HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 5);
			for (IntType value : hyperSphere)
				try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
		}

		System.out.println("img done");

		//create labeling
		ImgLabeling<IntType, IntType> labeling = ij.op().labeling().cca(img, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		System.out.println("cca done");

		TimeSliceLabelEditorModel model = new TimeSliceLabelEditorModel(labeling, 3);
//
//		ij.ui().show(model);

		LabelEditorView<Integer> view = new LabelEditorView<>(model);
		view.renderers().addDefaultRenderers();

		JPanel viewer = new JPanel(new MigLayout());
		JFrame frame = new JFrame("Label editor");
		BdvHandlePanel panel = new BdvHandlePanel(frame, Bdv.options().is2D());
//		BdvFunctions.show(input, "RAW", Bdv.options().addTo(panel));
		view.renderers().forEach(renderer -> BdvFunctions.show(renderer.getOutput(), renderer.getName(), Bdv.options().addTo(panel)));

		viewer.add( panel.getViewerPanel(), "span, grow, push" );
		BdvInterface.control(model, view, panel);

		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(viewer);
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String... args) throws IOException {
		new E09_4DLabeling().run();
	}


}
