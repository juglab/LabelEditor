package com.indago.labeleditor.application;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

@Plugin(type = Command.class, name = "ImgLabeling viewer")
public class ImgLabelingViewer<L> implements Command {

	@Parameter
	Context context;

	@Parameter(required = false)
	ImgPlus data;

	@Parameter
	ImgLabeling<L, IntType> labeling;

	@Override
	public void run() {
		LabelEditorBdvPanel<L> panel = new LabelEditorBdvPanel<>();
		context.inject(panel);
		panel.init(labeling, data);
		JFrame frame = new JFrame();
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		Img<IntType> threshold = (Img) ij.op().threshold().otsu(input);
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(threshold, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);
		ij.command().run(ImgLabelingViewer.class, true, "data", input, "labeling", labeling);
	}
}
