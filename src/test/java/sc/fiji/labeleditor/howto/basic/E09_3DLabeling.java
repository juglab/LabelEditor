package sc.fiji.labeleditor.howto.basic;

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
 * How a 3D labeling looks like in the LabelEditor
 */
public class E09_3DLabeling {

	public void run() {
		ImageJ ij = new ImageJ();
		ij.launch();

		//create data
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

		LabelEditorBdvPanel<IntType> panel = new LabelEditorBdvPanel<>();
		panel.setMode3D(true);
		ij.context().inject(panel);
		panel.init(labeling);

		panel.getSources().forEach(source -> source.setDisplayRange(0, 100));

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
