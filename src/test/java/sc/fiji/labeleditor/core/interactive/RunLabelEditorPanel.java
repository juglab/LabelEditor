package sc.fiji.labeleditor.core.interactive;

import io.scif.img.IO;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.cache.img.DiskCachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;
import java.awt.*;

public class RunLabelEditorPanel {

	private static String LABEL1 = "label1";
	private static String LABEL2 = "label2";

	private static String TAG1 = "tag1";
	private static String TAG2 = "tag2";

	public static void main(String... args) {
		ImgPlus img = buildData();
		LabelEditorModel model = buildModel(img);
		model.colors().getFaceColor(TAG1).set(255,255,0,50);
		model.colors().getFaceColor(TAG2).set(0,255,255,50);
		model.setData(img);
		JFrame frame = new JFrame("Label editor");
		JPanel parent = new JPanel();
		frame.setContentPane(parent);
		frame.setMinimumSize(new Dimension(500,500));
		LabelEditorBdvPanel labelEditorPanel = new LabelEditorBdvPanel<>();
		labelEditorPanel.init(model);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.add(labelEditorPanel);
		frame.pack();
		frame.setVisible(true);
	}

	private static <T extends RealType<T> & NativeType<T>> ImgPlus<T> buildData() {
		Img input = IO.openImgs(LabelEditorBdvPanel.class.getResource("/raw.tif").getPath()).get(0);
		return new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y, Axes.TIME});
	}

	private static <T extends RealType<T> & NativeType<T>> LabelEditorModel<String> buildModel(ImgPlus data) {

		DiskCachedCellImg<IntType, ?> backing = new DiskCachedCellImgFactory<>(new IntType()).create( data.dimension(0), data.dimension(1), data.dimension(2) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		Views.interval( labels, Intervals.createMinSize( 220, 220, 0, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 280, 280, 0, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL2 ) );

		Views.interval( labels, Intervals.createMinSize( 320, 320, 1, 100, 100, 1 ) ).forEach(pixel -> pixel.add( LABEL2 ) );
		Views.interval( labels, Intervals.createMinSize( 300, 300, 1, 100, 100, 1 ) ).forEach( pixel -> pixel.add( LABEL1 ) );

		LabelEditorModel<String> model = new DefaultLabelEditorModel<>(labels);
		model.tagging().addTagToLabel(LABEL1, TAG1);
		model.tagging().addTagToLabel(LABEL2, TAG2);
		return model;
	}
}
