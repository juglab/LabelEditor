package sc.fiji.labeleditor.application;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

@Plugin(type = Command.class, name = "Mask channels viewer")
public class MaskChannelsViewer implements Command {

	@Parameter
	private ImgPlus data;

	@Parameter
	private int channelDim;

	@Parameter
	private int channelSource;

	@Parameter
	private OpService ops;
	
	@Parameter
	private Context context;

	@Override
	public void run() {
		printDims(data);
		ArrayImg<IntType, IntArray> backing = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( Views.hyperSlice(data, channelDim, 0) );
//		Img<IntType> backing = new DiskCachedCellImgFactory<>(new IntType()).create(Views.hyperSlice(data, channelDim, 0)); // TODO why can I not do this?
		ImgLabeling< String, IntType > labeling = new ImgLabeling<>( backing );
		Img dataImg = new DiskCachedCellImgFactory<>(new IntType()).create(Views.hyperSlice(data, channelDim, 0));
		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labeling, dataImg);
		Random random = new Random();
		for (int i = 0; i < data.dimension(channelDim); i++) {
			System.out.println(i);
			IntervalView slice = Views.hyperSlice(data, channelDim, i);
			if(i == channelSource) {
				ops.copy().rai(dataImg, slice);
			} else {
				Img bitSlice = ops.convert().bit(slice);
				ArrayImg<IntType, IntArray> backingSlice = (ArrayImg<IntType, IntArray>) new ArrayImgFactory<>(new IntType()).create( bitSlice );
				ImgLabeling< String, IntType > labelingSlice = new ImgLabeling<>( backingSlice );
				ops.labeling().cca(labelingSlice, bitSlice, ConnectedComponents.StructuringElement.EIGHT_CONNECTED, new LabelGenerator(i + "_"));
				Integer tag = i;
				int randomColor = ARGBType.rgba(random.nextInt(155) + 100, random.nextInt(155) + 100, random.nextInt(255) + 100, 100);
				model.colors().getFaceColor(tag).set(randomColor);
				labelingSlice.getMapping().getLabels().forEach(labelset -> {
					model.tagging().addTagToLabel(tag, labelset);
				});
				ops.labeling().merge(labeling, labeling, labelingSlice);
			}
		}
		printDims(labeling);
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel();
		context.inject(panel);
		panel.add(model);
		JFrame frame = new JFrame();
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);
	}

	private void printDims(RandomAccessibleInterval data) {
		long[] dims = new long[data.numDimensions()];
		data.dimensions(dims);
		System.out.println(Arrays.toString(dims));
	}

	public static void main(String... args) throws IOException {
		ImageJ ij = new ImageJ();
		ij.launch();
		final File file = ij.ui().chooseFile(null, "open");
		if(file != null && file.exists()) {
			Img input = (Img) ij.io().open(file.getAbsolutePath());
			ij.command().run(MaskChannelsViewer.class, true, "data", input, "channelDim", 2, "channelSource", 2);
		}
	}
}
