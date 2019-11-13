package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * How to add custom actions to set or remove tags to {@link LabelEditorBdvPanel}
 * TODO this example is quite buggy, fix it, make it use behaviours
 */
public class E08_CustomActions {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<Integer> panel;

	class PopUpDemo extends JPopupMenu {
		JMenuItem anItem;
		public PopUpDemo(LabelEditorPanel<Integer> panel) {
			anItem = new JMenuItem("Click Me!");
			anItem.addActionListener(actionEvent -> {
				System.out.println("Event!");
				Set<Integer> labels = panel.model().tagging().getLabels("no");
				//TODO start collect tagging events, pause listeners
				labels.forEach(label -> panel.model().tagging().addTag("special", label));
//				Views.interval( panel.getModel().getLabels(), Intervals.createMinSize( mouse.getIntPosition(0), mouse.getIntPosition(1), 10, 10 ) ).forEach(pixel -> pixel.add( 100 ) );
				//TODO resume model listeners and send collected events

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

		//open blobs
		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());

		//do connected component analysis
		Img thresholded = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		// build LabelEditorPanel
		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		//set custom colors for tags set in the MouseAdapter
		model.colors().remove(LabelEditorTag.SELECTED);
		model.colors().remove(LabelEditorTag.MOUSE_OVER);
		model.colors().get("yes").put(LabelEditorTargetComponent.FACE, ARGBType.rgba(155, 155, 0, 255));
		model.colors().get("no").put(LabelEditorTargetComponent.FACE, ARGBType.rgba(0, 155, 255, 255));
		model.colors().get("special").put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255, 0, 0, 255));

		panel = new LabelEditorBdvPanel<>();
		panel.init(new ImgPlus<>(input), model);

		//register custom actions
		panel.getInterfaceHandle().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				model.tagging().removeTag("no");
				for (Integer label : panel.control().interfaceInstance().getLabelsAtMousePosition(e, model)) {
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
				for (Integer label : panel.control().interfaceInstance().getLabelsAtMousePosition(e, model)) {
					model.tagging().addTag("no", label);
				}
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
