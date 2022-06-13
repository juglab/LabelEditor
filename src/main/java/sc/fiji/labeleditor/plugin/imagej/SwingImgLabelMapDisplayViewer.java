/*-
 * #%L
 * UI component for image segmentation label comparison and selection
 * %%
 * Copyright (C) 2019 - 2020 DAIS developers
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package sc.fiji.labeleditor.plugin.imagej;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.Priority;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.viewer.DisplayViewer;
import sc.fiji.labeleditor.application.LabelMap;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class creates a {@link LabelEditorBdvPanel} for a label map.
 */
@Plugin(type = DisplayViewer.class, priority = Priority.HIGH)
public class SwingImgLabelMapDisplayViewer<I extends IntegerType<I>> extends EasySwingDisplayViewer<LabelMap> implements Disposable {

	@Parameter
	private Context context;

	@Parameter
	private OpService opService;

	private Random random = new Random();
	private LabelEditorBdvPanel panel;

	public SwingImgLabelMapDisplayViewer() {
		super(LabelMap.class);
	}

	@Override
	protected boolean canView(LabelMap labelMap) {
		return true;
	}

	@Override
	protected JPanel createDisplayPanel(LabelMap labelMap) {
		if(labelMap.hasChannels()) {
			List<RandomAccessibleInterval<? extends IntegerType<?>>> labelings = new ArrayList<>();
			for (int i = 0; i < labelMap.dimension(labelMap.numDimensions()-1); i++) {
				labelings.add(Views.hyperSlice(labelMap, labelMap.numDimensions()-1, i));
			}
			panel = makePanel(labelings, labelMap.getRaws());
		} else {
			panel = makePanel(labelMap);
		}
		return panel;
	}

	private LabelEditorBdvPanel makePanel(List<RandomAccessibleInterval<? extends IntegerType<?>>> labelings, List<RandomAccessibleInterval<? extends RealType<?>>> rest) {
		BdvOptions options = new BdvOptions();
		if(labelings.get(0).numDimensions() == 2
				|| (labelings.get(0).numDimensions() > 2
				&& labelings.get(0).dimension(2) == 1)) {
			options.is2D();
		}
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(context, options);
		for (int i = 0; i < rest.size(); i++) {
			BdvStackSource<? extends RealType<?>> source = BdvFunctions.show(rest.get(i), "data " + i, new BdvOptions().addTo(panel.getBdvHandlePanel()));
			source.setDisplayRange(0, 255);
		}
		for (RandomAccessibleInterval<? extends IntegerType<?>> labelMap : labelings) {
			DefaultLabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap);
			setRandomColors(model);
			panel.add(model);
		}
		return panel;
	}

	private void setRandomColors(DefaultLabelEditorModel model) {
		double split1 = random.nextDouble();
		double split2 = random.nextDouble();
		int randomFaceColor = ARGBType.rgba(255*split1, 255*split2, 255*(1-split1-split2), 100);
		int randomBorderColor = ARGBType.rgba(255*split1, 255*split2, 255*(1-split1-split2), 200);
		model.colors().getDefaultFaceColor().set(randomFaceColor);
		model.colors().getDefaultBorderColor().set(randomBorderColor);
		model.colors().getSelectedFaceColor().set(0xddddddff);
		model.colors().getSelectedBorderColor().set(0xffffffff);
	}

	private <I extends IntegerType<I>> LabelEditorBdvPanel makePanel(RandomAccessibleInterval<I> labelMap) {
		BdvOptions options = new BdvOptions();
		if(labelMap.numDimensions() == 2
				|| (labelMap.numDimensions() > 2
				&& labelMap.dimension(2) == 1)) {
			options.is2D();
		}
		DefaultLabelEditorModel<IntType> model = DefaultLabelEditorModel.initFromLabelMap(labelMap);
		LabelEditorBdvPanel panel = new LabelEditorBdvPanel(context, options);
		panel.add(model);
		return panel;
	}

//	private <I extends IntegerType<I>> ImgLabeling<Integer, I> makeLabeling(RandomAccessibleInterval<I> labelMap) {
//		List<Pair<Integer, Set<Integer>>> maxLabelsPairs = LoopBuilder.setImages(labelMap).multiThreaded().forEachChunk(chunk -> {
//			AtomicReference<Integer> max = new AtomicReference<>(0);
//			Set<Integer> labelset= new HashSet<>();
//			chunk.forEachPixel(pixel -> {
//				if(max.get() < pixel.getInteger()) max.set(pixel.getInteger());
//				labelset.add(pixel.getInteger());
//			});
//			Pair<Integer, Set<Integer>> pair = new ValuePair<>(max.get(), labelset);
//			return pair;
//		});
//		Set<Integer> uniqueLabels = new HashSet<>();
//		AtomicReference<Integer> max = new AtomicReference<>(0);
//		maxLabelsPairs.forEach(maxLabelsPair -> {
//			if(maxLabelsPair.getA() > max.get()) max.set(maxLabelsPair.getA());
//			uniqueLabels.addAll(maxLabelsPair.getB());
//		});
//		List<Integer> labels = new ArrayList<>();
//		for (int i = 1; i < max.get()+1; i++) {
//			labels.add(i);
//		}
//		return ImgLabeling.fromImageAndLabels(labelMap, labels);
//	}

	@Override
	public void redraw()
	{
		//TODO do I need to update the panel / create a new panel?
		getWindow().pack();
	}

	@Override
	public void redoLayout()
	{
		// ignored
	}

	@Override
	public void setLabel(final String s)
	{
		// ignored
	}

	@Override
	public void dispose() {
		panel.dispose();
		super.dispose();
	}
}
