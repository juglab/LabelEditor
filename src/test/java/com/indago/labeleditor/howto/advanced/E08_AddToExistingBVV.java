package com.indago.labeleditor.howto.advanced;

import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorRenderer;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.plugin.interfaces.bvv.BvvInterface;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class E08_AddToExistingBVV {

	public <T extends RealType<T>> void run() throws IOException {

		ImageJ ij = new ImageJ();
		ij.launch();

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		RandomAccessibleInterval inputStack = input;
		List<RandomAccessibleInterval<T>> stack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			stack.add(inputStack);
		}
		inputStack = Views.stack(stack);
		Img binary = (Img) ij.op().threshold().otsu(Views.iterable(inputStack));
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(binary, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		LabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);
		model.colors().getDefaultFaceColor().set(255,255,0,55);

		LabelEditorView<Integer> view = new LabelEditorView<>(model);
		view.renderers().addDefaultRenderers();

		List<BvvStackSource> sources = new ArrayList<>();
		BvvStackSource source = null;
		for (LabelEditorRenderer renderer : view.renderers()) {
			RandomAccessibleInterval img = renderer.getOutput();
			if (source == null) {
				source = BvvFunctions.show(img, renderer.getName(), Bvv.options());
			} else {
				source = BvvFunctions.show(img, renderer.getName(), Bvv.options().addTo(source.getBvvHandle()));
			}
			sources.add(source);
		}

		BvvInterface.control(model, view, source.getBvvHandle(), sources);

	}

	public static void main(String... args) throws IOException {
		new E08_AddToExistingBVV().run();
	}


}