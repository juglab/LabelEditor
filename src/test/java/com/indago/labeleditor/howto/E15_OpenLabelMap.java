package com.indago.labeleditor.howto;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.core.view.LabelEditorTargetComponent;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.ARGBType;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class E15_OpenLabelMap {

	public void run() throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open(getClass().getResource("/labelmap.png").getPath());
		LabelEditorPanel panel = new LabelEditorBdvPanel();
		ij.context().inject(panel);
		panel.initFromIndexImage(input);
		panel.model().colors().get(LabelEditorTag.DEFAULT).put(LabelEditorTargetComponent.FACE, ARGBType.rgba(255,255,255,10));
		JFrame frame = new JFrame("Label editor");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String...args) throws IOException {
		new E15_OpenLabelMap().run();
	}

}
