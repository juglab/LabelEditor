package com.indago.labeleditor.howto;


import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.behaviours.select.ConflictSelectionBehaviours;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import javax.swing.*;
import java.awt.*;

/**
 * How to open an {@link ImgLabeling} in a {@link LabelEditorBdvPanel}.
 */
public class E13_HandleMultipleConflicts {

	public void run() {

		ImageJ ij = new ImageJ();
		ij.launch();

		String LABEL1 = "label1";
		String LABEL2 = "label2";
		String LABEL3 = "label3";
		String LABEL4 = "label4";
		String LABEL5 = "label5";
		String LABEL6 = "label6";
		String LABEL7 = "label7";
		String LABEL8 = "label8";
		String LABEL9 = "label9";

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( 500, 500 );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );

		int radius = 50;
		drawSphere(labels, new long[]{170, 170}, radius, LABEL1);
		drawSphere(labels, new long[]{170, 200}, radius, LABEL2);
		drawSphere(labels, new long[]{200, 200}, radius, LABEL3);
		drawSphere(labels, new long[]{200, 170}, radius, LABEL4);

		drawSphere(labels, new long[]{240, 280}, (int) (radius*1.2), LABEL9);

		drawSphere(labels, new long[]{270, 370}, radius, LABEL5);
		drawSphere(labels, new long[]{270, 400}, radius, LABEL6);
		drawSphere(labels, new long[]{300, 400}, radius, LABEL7);
		drawSphere(labels, new long[]{300, 370}, radius, LABEL8);
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>();
		model.init(labels);

		model.tagging().addTag(LabelEditorTag.SELECTED, LABEL2);
		model.tagging().addTag(LabelEditorTag.SELECTED, LABEL7);
		model.colors().get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.BORDER, ARGBType.rgba(0,255,255,100));
		model.colors().get(LabelEditorTag.DEFAULT).remove(LabelEditorTargetComponent.FACE);
		model.colors().get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.BORDER, ARGBType.rgba(0,255,255,100));
//		model.colors().get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(50,50,50,100));

		LabelEditorBdvPanel<String> panel = new LabelEditorBdvPanel<>();
		ij.context().inject(panel);
		panel.init(model);
		panel.control().install(new ConflictSelectionBehaviours<>());
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	private void drawSphere(ImgLabeling<String, IntType> img, long[] position, int radius, String label) {
		RandomAccess<LabelingType<String>> ra = img.randomAccess();
		ra.setPosition(position);
		HyperSphere<LabelingType<String>> hyperSphere = new HyperSphere<>(img, ra, radius);
		for (LabelingType<String> value : hyperSphere)
			value.add(label);
	}

	public static void main(String... args) {
		new E13_HandleMultipleConflicts().run();
	}

}
