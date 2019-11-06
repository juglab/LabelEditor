package com.indago.labeleditor.howto;

import com.indago.labeleditor.LabelEditorBdvPanel;
import com.indago.labeleditor.LabelEditorPanel;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.Localizable;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
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
public class E08_CustomActions {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<Integer> panel;

	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		public PopUpDemo(LabelEditorPanel<Integer> panel, Localizable mouse) {
			anItem = new JMenuItem("Click Me!");
			anItem.addActionListener(actionEvent -> {
				System.out.println("Event!");
				List<Integer> labels = panel.model().tagging().getLabels(LabelEditorTag.SELECTED);
				labels.forEach(label -> panel.model().tagging().addTag("special", label));
//				Views.interval( panel.getModel().getLabels(), Intervals.createMinSize( mouse.getIntPosition(0), mouse.getIntPosition(1), 10, 10 ) ).forEach(pixel -> pixel.add( 100 ) );
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

		// build LabelEditorPanel
		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		panel = new LabelEditorBdvPanel<>();
		panel.init(new ImgPlus<>(input), model);

		//set custom colors for tags set in the MouseAdapter
		panel.rendering().removeTagColor(LabelEditorTag.SELECTED);
		panel.rendering().removeTagColor(LabelEditorTag.MOUSE_OVER);
		panel.rendering().setTagColor("yes", ARGBType.rgba(155, 155, 0, 255));
		panel.rendering().setTagColor("no", ARGBType.rgba(0, 155, 255, 255));
		panel.rendering().setTagColor("special", ARGBType.rgba(255, 0, 0, 255));
		panel.updateLabelRendering();

		//register custom actions
		panel.bdvGetHandlePanel().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e, panel.action().getDataPositionAtMouse());
				model.tagging().removeTag("no");
				for (Integer label : panel.action().getLabelsAtMousePosition(e)) {
					model.tagging().addTag("yes", label);
				}
				panel.updateLabelRendering();
				super.mousePressed(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger())
					doPop(e, panel.action().getDataPositionAtMouse());
				model.tagging().removeTag("yes");
				for (Integer label : panel.action().getLabelsAtMousePosition(e)) {
					model.tagging().addTag("no", label);
				}
				panel.updateLabelRendering();
				super.mouseReleased(e);
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
		new E08_CustomActions().mouseAction();
	}

}
