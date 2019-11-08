package com.indago.labeleditor.howto;

import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.action.ActionManager;
import com.indago.labeleditor.core.display.RenderingManager;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.plugin.bvv.BvvViewerInstance;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.img.Img;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.junit.AfterClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class E06_AddToExistingBVV {

	static ImageJ ij = new ImageJ();

	@Test
	public <T extends RealType<T>> void run() throws IOException {

		Img input = (Img) ij.io().open(getClass().getResource("/blobs.png").getPath());
		RandomAccessibleInterval inputStack = input;
		List<RandomAccessibleInterval<T>> stack = new ArrayList<>();
		for (int i = 0; i < 50; i++) {
			stack.add(inputStack);
		}
		inputStack = Views.stack(stack);
		Img thresholded = (Img) ij.op().threshold().otsu(Views.iterable(inputStack));
		ImgLabeling<Integer, IntType> labeling = ij.op().labeling().cca(thresholded, ConnectedComponents.StructuringElement.EIGHT_CONNECTED);

		DefaultLabelEditorModel<Integer> model = new DefaultLabelEditorModel<>(labeling);

		RenderingManager<Integer> renderer = new RenderingManager<>(model);
		renderer.addDefaultRenderings();
		model.labelRegions().forEach((label, regions) -> {
			model.tagging().addTag("displayed", label);
		});
		renderer.setTagColor("displayed", ARGBType.rgba(255,255,0,55));
		renderer.update();
		//add to BVV
//		BvvStackSource<ARGBType> source1 = BvvFunctions.show(imgArgb, "RAW", Bvv.options());
		List<BvvStackSource> sources = new ArrayList<>();
		BvvStackSource source = BvvFunctions.show(renderer.getRenderings().get(0), "labels", Bvv.options());
		sources.add(source);

		ActionManager<Integer> actionHandler = new ActionManager<>();
		actionHandler.init(new BvvViewerInstance(source.getBvvHandle(), sources), model, renderer);
		actionHandler.addDefaultActionHandlers();
	}


	@AfterClass
	public static void dispose() {
		ij.context().dispose();
	}

	public static void main(String... args) throws IOException {
		new E06_AddToExistingBVV().run();
	}


}
