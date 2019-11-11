package com.indago.labeleditor.howto;


import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.behaviours.ConflictSelectionBehaviours;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imglib2.RandomAccess;
import net.imglib2.algorithm.region.hypersphere.HyperSphere;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

/**
 * How to open an {@link ImgLabeling} in a {@link LabelEditorBdvPanel}.
 */
public class E13_HandleMultipleConflicts {

	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<String> panel;

	@Test
	@Ignore
	public void run() {

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

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);

		model.tagging().addTag(LabelEditorTag.SELECTED, LABEL2);
		model.tagging().addTag(LabelEditorTag.SELECTED, LABEL7);

		panel = new LabelEditorBdvPanel<String>() {
			@Override
			protected void addBehaviours(LabelEditorController<String> controller) {
				new ConflictSelectionBehaviours<>(model, panel.control()).install(controller.interfaceInstance().behaviours(), panel);
			}
		};
		panel.init(model);
		panel.view().colors().get(LabelEditorTag.SELECTED).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255,0,0,200));
		panel.view().colors().get(LabelEditorTag.NO_TAG).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255,255,255,50));
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

	@AfterClass
	public static void dispose() {
		frame.dispose();
		panel.dispose();
	}

	public static void main(String... args) {
		new E13_HandleMultipleConflicts().run();
	}

}
