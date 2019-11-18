package com.indago.labeleditor.application;

import net.imagej.ops.OpService;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import org.scijava.ItemIO;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, name = "Connected Component Analysis")
public class ConnectedComponentAnalysis implements Command {

	@Parameter
	Img<BitType> binaryInput;

	//TODO hack to make the display viewer not fail on trying to display this
	@Parameter(required = false)
	private ImgLabeling output;

	@Parameter(description = "CCA structuring element", choices = {"four-connected", "eight-connected"})
	String structuringElementChoice;

	@Parameter
	OpService opService;

	@Override
	public void run() {

		ConnectedComponents.StructuringElement structuringElement = ConnectedComponents.StructuringElement.FOUR_CONNECTED;
		if(structuringElementChoice.equals("eight-connected")) {
			structuringElement = ConnectedComponents.StructuringElement.EIGHT_CONNECTED;
		}
		output = opService.labeling().cca(binaryInput, structuringElement);
	}
}
