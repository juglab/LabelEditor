package com.indago.labeleditor.application;

import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.Dataset;
import net.imagej.DefaultDataset;
import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imagej.plugins.commands.binary.Binarize;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandModule;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@Plugin(type = Command.class, name = "Objects Counter (IJ2)")
public class ObjectsCounter<L> implements Command {

	@Parameter
	ImgPlus data;

	@Parameter
	OpService opService;

	@Parameter
	Context context;

	@Parameter
	CommandService commandService;

	@Parameter
	boolean invertImg = false;

	@Override
	public void run() {

		try {
			Img img = data.getImg();
			if(invertImg) {
				img = opService.copy().img(data.getImg());
				opService.image().invert(img, data.getImg());
			}
			Dataset dataset = new DefaultDataset(context, new ImgPlus<>(img));
			CommandModule thresholdCommand = commandService.run(Binarize.class, true, "inputData", dataset).get();
			Dataset binaryData = (Dataset) thresholdCommand.getOutput("outputMask");
			Img<BitType> binaryImg = (Img<BitType>) binaryData.getImgPlus().getImg();
			CommandModule ccaCommand = commandService.run(ConnectedComponentAnalysis.class, true, "binaryInput", binaryImg).get();
			ImgLabeling<Integer, IntType> labeling = (ImgLabeling<Integer, IntType>) ccaCommand.getInput("output");
			LabelEditorBdvPanel<Integer> panel = new LabelEditorBdvPanel<>();
			context.inject(panel);
			panel.init(labeling, data);
			JFrame frame = new JFrame();
			frame.setContentPane(panel.get());
			frame.setMinimumSize(new Dimension(500,500));
			frame.pack();
			frame.setVisible(true);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		Img input = (Img) ij.io().open("https://samples.fiji.sc/blobs.png");
		ij.command().run(ObjectsCounter.class, true, "data", input);
	}
}
