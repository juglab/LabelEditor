package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.behaviours.ModificationBehaviours;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
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

/**
 * How to add custom actions to set or remove tags to {@link LabelEditorBdvPanel}
 */
public class E11_ChangingLabelingOnAction {

	static ImageJ ij = new ImageJ();
	static JFrame frame = new JFrame("Label editor");
	static LabelEditorBdvPanel<Integer> panel;

	static class PopUpDemo extends JPopupMenu {

		PopUpDemo(LabelEditorPanel<Integer> panel, ModificationBehaviours modificationBehaviours) {
			JMenuItem item = new JMenuItem("Remove");
			item.addActionListener(actionEvent -> {
				modificationBehaviours.getDeleteBehaviour().deleteSelected();
			});
			add(item);
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

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>();
		model.init(labeling, input);
		model.labels().getMapping().getLabels().forEach(label -> model.tagging().addTag("displayed", label));
		model.colors().get("displayed").put(LabelEditorTargetComponent.FACE, ARGBType.rgba(0,255,255,155));

		// build LabelEditorPanel
		panel = new LabelEditorBdvPanel<>();
		panel.init(model);

		//register custom actions
		ModificationBehaviours modificationBehaviours = new ModificationBehaviours();
		modificationBehaviours.init(model, panel.control());
		panel.getInterfaceHandle().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (e.isPopupTrigger()) {
					PopUpDemo menu = new PopUpDemo(panel, modificationBehaviours);
					menu.show(e.getComponent(), e.getX(), e.getY());
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
		new E11_ChangingLabelingOnAction().mouseAction();
	}

}
