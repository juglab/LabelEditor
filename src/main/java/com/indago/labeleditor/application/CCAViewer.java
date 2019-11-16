package com.indago.labeleditor.application;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
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

@Plugin(type = Command.class, name = "CCA viewer")
public class CCAViewer<L> implements Command {

	@Parameter
	ImgPlus data;

	@Parameter(description = "CCA structuring element", choices = {"four-connected", "eight-connected"})
	String structuringElementChoice;

	@Parameter
	OpService opService;

	@Parameter
	Context context;

	@Override
	public void run() {
		Img threshold = (Img) opService.threshold().otsu(data);
		ConnectedComponents.StructuringElement structuringElement = ConnectedComponents.StructuringElement.FOUR_CONNECTED;
		if(structuringElementChoice.equals("eight-connected")) {
			structuringElement = ConnectedComponents.StructuringElement.EIGHT_CONNECTED;
		}
		ImgLabeling<Integer, IntType> labeling = opService.labeling().cca(threshold, structuringElement);

		LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
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
		ij.launch();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		ij.command().run(CCAViewer.class, true, "data", input);
	}
}
