package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * How to add custom actions to set or remove tags to {@link LabelEditorBdvPanel}
 */
public class E08_CustomActions {

	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		public PopUpDemo() {
			anItem = new JMenuItem("Click Me!");
			anItem.addActionListener(actionEvent -> {
				System.out.println("Event!");
			});
			add(anItem);
		}
	}

	/**
	 * Demonstrates how to register a mouse action.
	 */
	@Test
	public void mouseAction() throws IOException {

		//initialize ImageJ
		ImageJ ij = new ImageJ();

		//open blobs
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");

		//do connected component analysis
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		// build LabelEditorPanel
		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>(new ImgPlus<>(input), model);

		//set custom colors for tags set in the MouseAdapter
		panel.getRenderer().removeTagColor(LabelEditorTag.SELECTED);
		panel.getRenderer().removeTagColor(LabelEditorTag.MOUSE_OVER);
		panel.getRenderer().setTagColor("yes", ARGBType.rgba(155, 155, 0, 255));
		panel.getRenderer().setTagColor("no", ARGBType.rgba(0, 155, 255, 255));
		panel.updateLabelRendering();

		//register custom actions
		panel.bdvGetHandlePanel().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
				model.removeTag("no");
				for (Integer label : panel.getActionHandler().getLabelsAtMousePosition(e)) {
					model.addTag("yes", label);
				}
				panel.updateLabelRendering();
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e);
				model.removeTag("yes");
				for (Integer label : panel.getActionHandler().getLabelsAtMousePosition(e)) {
					model.addTag("no", label);
				}
				panel.updateLabelRendering();
				super.mouseReleased(e);
			}

			private void doPop(MouseEvent e) {
				PopUpDemo menu = new PopUpDemo();
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		});

		//build frame
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel);
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E08_CustomActions().mouseAction();
	}

}
