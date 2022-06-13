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
package sc.fiji.labeleditor.plugin.behaviours.export;

import net.imagej.ops.OpService;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionCursor;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportLabels<L> implements Behaviour {

	@Parameter
	UIService ui;

	@Parameter
	OpService ops;

	private final LabelEditorModel<L> model;

	public ExportLabels(LabelEditorModel<L> model) {
		this.model = model;
	}

	public void exportSelected() {
		List<L> selected = model.tagging().getLabels(LabelEditorTag.SELECTED);

		LabelRegions<L> regions = new LabelRegions<>(model.labeling());
		Map<L, LabelRegion<L>> regionList = new HashMap<>();

		selected.forEach(label -> regionList.put(label, regions.getLabelRegion(label)));
		Interval boundingBox = null;
		for (LabelRegion<L> region : regionList.values()) {
			if (boundingBox == null) {
				boundingBox = region;
			} else {
				boundingBox = Intervals.union(boundingBox, region);
			}
		}

		ImgLabeling<L, IntType> cropLabeling = createCroppedLabeling(selected, boundingBox, regionList);

		LabelEditorModel<L> exportModel;
		if(model.getData() != null) {
			RandomAccessibleInterval data = createCroppedData(boundingBox);
			exportModel = new DefaultLabelEditorModel<>(cropLabeling, data);
		} else {
			exportModel = new DefaultLabelEditorModel<>(cropLabeling);
		}
		for(L label : exportModel.labeling().getMapping().getLabels()) {
			List<Object> oldTags = model.tagging().getTags(label);
			oldTags.forEach(tag -> exportModel.tagging().addTagToLabel(tag, label));
		}

		ui.show(exportModel);

	}

	private RandomAccessibleInterval createCroppedData(Interval boundingBox) {
		return ops.copy().rai(Views.zeroMin(Views.interval(model.getData(), boundingBox)));
	}

	private ImgLabeling<L, IntType> createCroppedLabeling(List<L> labels, Interval boundingBox, Map<L, LabelRegion<L>> regionList) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create( boundingBox );
		ImgLabeling<L, IntType> cropLabeling = new ImgLabeling<>(backing);
		Point offset = new Point(boundingBox.numDimensions());
		for (int i = 0; i < boundingBox.numDimensions(); i++) {
			offset.setPosition(-boundingBox.min(i), i);
		}
		RandomAccess<LabelingType<L>> outRA = cropLabeling.randomAccess();
		labels.forEach(label -> {
			LabelRegion<L> region = regionList.get(label);
			LabelRegionCursor inCursor = region.localizingCursor();
			while(inCursor.hasNext()) {
				inCursor.next();
				outRA.setPosition(inCursor);
				outRA.move(offset);
				outRA.get().add(label);
			}
		});
		return cropLabeling;
	}
}
