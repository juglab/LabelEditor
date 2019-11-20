package com.indago.labeleditor.application;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.IOException;

@Plugin(type = Command.class, name = "Watershed viewer")
public class WatershedViewer<L> implements Command {

	@Parameter
	ImgPlus data;

	@Parameter(type = ItemIO.OUTPUT)
	ImgLabeling labeling;

	@Parameter(description = "CCA structuring element", choices = {"four-connected", "eight-connected"})
	String structuringElementChoice;

	@Parameter
	OpService opService;

	@Override
	public void run() {
		boolean eightConnected = structuringElementChoice.equals("eight-connected");
		labeling = opService.image().watershed(data, eightConnected, false);
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		ij.command().run(WatershedViewer.class, true, "data", input);
	}
}
