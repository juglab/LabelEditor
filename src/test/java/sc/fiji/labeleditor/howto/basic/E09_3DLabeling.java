package sc.fiji.labeleditor.howto.basic;

import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Random;

/**
 * How to active the 3D mode of the LabelEditor
 */
public class E09_3DLabeling {

	public void run() {
		ImageJ ij = new ImageJ();
		ij.launch();

		//create image with spheres at random positions
		Img<IntType> img = new ArrayImgFactory<>(new IntType()).create(500, 500, 500);
		RandomAccess<IntType> ra = img.randomAccess();
		Random random = new Random();
		for (int i = 0; i < 42; i++) {
			System.out.println(i);
			ra.setPosition(new int[]{random.nextInt(500), random.nextInt(500), random.nextInt(500)});
			HyperSphere<IntType> hyperSphere = new HyperSphere<>(img, ra, 42);
			for (IntType value : hyperSphere)
				try{value.set(ra.getIntPosition(0));} catch(ArrayIndexOutOfBoundsException e) {}
		}

		//create labeling
		ImgLabeling<IntType, IntType> labeling = ij.op().labeling().cca(img, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		// for 3D mode, one cannot use the ui service yet, so we create our own panel..
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel();

		// .. and enable the 3D mode
		panel.setMode3D(true);

		// (don't forget to inject the context to get all the IJ2 goodies, but it should also work (with a limited set of features) without this)
		ij.context().inject(panel);

		// initialize the panel..
		panel.add(new DefaultLabelEditorModel<>(labeling));

		// .. maybe set the display range for the inputs..
		panel.getSources().forEach(source -> source.setDisplayRange(0, 100));

		// .. and create a frame to show the panel.
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);

	}

	public static void main(String... args) throws IOException {
		new E09_3DLabeling().run();
	}


}
