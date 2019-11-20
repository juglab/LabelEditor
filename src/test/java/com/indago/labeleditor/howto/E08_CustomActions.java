package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Set;

/**
 * How to add custom actions to set or remove tags to {@link LabelEditorBdvPanel}
 * TODO this example is quite buggy, fix it, make it use behaviours
 */
public class E08_CustomActions {

	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		public PopUpDemo(LabelEditorPanel<Integer> panel) {
			anItem = new JMenuItem("Click Me!");
			anItem.addActionListener(actionEvent -> {
				System.out.println("Event!");
				Set<Integer> labels = panel.model().tagging().getLabels("no");
				panel.model().tagging().pauseListeners();
				labels.forEach(label -> panel.model().tagging().addTag("special", label));
//				Views.interval( panel.getModel().getLabels(), Intervals.createMinSize( mouse.getIntPosition(0), mouse.getIntPosition(1), 10, 10 ) ).forEach(pixel -> pixel.add( 100 ) );
				panel.model().tagging().resumeListeners();
			});
			add(anItem);
		}
	}

	/**
	 * Demonstrates how to register a mouse action.
	 */
	@Test
	@Ignore
	public void mouseAction() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		//open blobs
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		//do connected component analysis
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		// build LabelEditorPanel
		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>();
		model.init(labeling);
		//set custom colors for tags set in the MouseAdapter
		model.colors().remove(LabelEditorTag.SELECTED);
		model.colors().remove(LabelEditorTag.FOCUS);
		model.colors().getFaceColor("yes").set(155, 155, 0);
		model.colors().getFaceColor("no").set(0, 155, 255);
		model.colors().getFaceColor("special").set(255, 0, 0);

		model.setData(input);

		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
		ij.context().inject(panel);
		panel.init(model);

		//register custom actions
		panel.getInterfaceHandle().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				model.tagging().removeTag("no");
				for (Integer label : panel.control().interfaceInstance().getLabelsAtMousePosition()) {
					model.tagging().addTag("yes", label);
				}
				if (e.isPopupTrigger()) {
					PopUpDemo menu = new PopUpDemo(panel);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				super.mouseReleased(e);
				model.tagging().removeTag("yes");
				for (Integer label : panel.control().interfaceInstance().getLabelsAtMousePosition()) {
					model.tagging().addTag("no", label);
				}
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
