package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.Localizable;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

/**
 * How to add custom actions to set or remove tags to {@link LabelEditorBdvPanel}
 */
public class E11_ChangingLabelingOnAction {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<Integer> panel;

	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		public PopUpDemo(LabelEditorPanel<Integer> panel, Localizable mouse) {
			anItem = new JMenuItem("Remove");
			anItem.addActionListener(actionEvent -> {
				List<Integer> labels = panel.model().tagging().getLabels(LabelEditorTag.MOUSE_OVER);
				Cursor<LabelingType<Integer>> cursor = panel.model().labels().cursor();
				while(cursor.hasNext()) {
					LabelingType<Integer> val = cursor.next();
					val.removeAll(labels);
				}
				panel.updateLabelRendering();

			});
			add(anItem);
		}
	}

	/**
	 * Demonstrates how to register a mouse action.
	 */
	@Test
	public void mouseAction() throws IOException {

		//open blobs
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		//do connected component analysis
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		model.labelRegions().forEach((label, regions) -> model.tagging().addTag("displayed", label));

		// build LabelEditorPanel
		panel = new LabelEditorBdvPanel<>();
		panel.init(new ImgPlus<>(input), model);
		panel.rendering().setTagColor("displayed", ARGBType.rgba(0,255,255,155));
		panel.updateLabelRendering();

		//register custom actions
		panel.getViewerHandle().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (e.isPopupTrigger()) {
					doPop(e, panel.action().getBridge().getDataPositionAtMouse());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				if (e.isPopupTrigger()) {
					doPop(e, panel.action().getBridge().getDataPositionAtMouse());
				}
			}

			private void doPop(MouseEvent e, Localizable dataPositionAtMouse) {
				PopUpDemo menu = new PopUpDemo(panel, dataPositionAtMouse);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		});

		//build frame
		frame.setContentPane(panel);
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

	public static void main(String...args) throws IOException {
		new E11_ChangingLabelingOnAction().mouseAction();
	}

}
