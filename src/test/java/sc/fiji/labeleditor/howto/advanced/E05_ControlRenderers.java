package sc.fiji.labeleditor.howto.advanced;

import net.imagej.ImageJ;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import sc.fiji.labeleditor.plugin.renderers.BorderLabelEditorRenderer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * How to control which renderers will be used by the LabelEditor
 */
public class E05_ControlRenderers {


	/**
	 * By default, all renderers of annotated as plugins of type {@link LabelEditorRenderer} will be added to the viewer.
	 * You can control this and only add specific renderers / your own renderers.
	 */
	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		Img binary = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel model = new DefaultLabelEditorModel(labeling, input);

		model.colors().getDefaultBorderColor().set(0,0,255);
		model.colors().getFocusBorderColor().set(0,255,0);
		model.colors().getSelectedBorderColor().set(255,0,0);

		// in this case, we do not try to find all existing renderers, but instead only add the border renderer
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(ij.context()) {
			@Override
			protected void addRenderers(LabelEditorView view) {
				view.renderers().add(new BorderLabelEditorRenderer<>());
			}
		};
		panel.add(model);

		JFrame frame = new JFrame("Label editor");
		frame.setMinimumSize(new Dimension(500,500));
		frame.setContentPane(panel.get());
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E05_ControlRenderers().run();
	}

}
