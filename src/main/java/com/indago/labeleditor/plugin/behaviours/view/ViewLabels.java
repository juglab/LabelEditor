package com.indago.labeleditor.plugin.behaviours.view;

import com.indago.labeleditor.core.LabelEditorPanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.LabelEditorTag;
import com.indago.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
import net.imagej.ImgPlus;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelRegion;
import net.imglib2.roi.labeling.LabelRegionCursor;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ViewLabels<L> implements Behaviour {

	private final LabelEditorController controller;
	private final LabelEditorModel<L> model;

	public ViewLabels(LabelEditorModel model, LabelEditorController controller) {
		this.model = model;
		this.controller = controller;
	}

	public void viewSelected() {
		Set<L> selected = model.tagging().getLabels(LabelEditorTag.SELECTED);

		LabelRegions regions = new LabelRegions<>(model.labels());
		Map<L, LabelRegion<L>> regionList = new HashMap<>();

		selected.forEach(label -> {
			regionList.put(label, regions.getLabelRegion(label));
		});
		Interval boundingBox = null;
		for (LabelRegion<L> region : regionList.values()) {
			if (boundingBox == null) {
				boundingBox = region;
			} else {
				boundingBox = Intervals.union(boundingBox, region);
			}
		}
//		Regions.sample()

		ImgLabeling<L, IntType> cropLabeling = createCroppedLabeling(selected, boundingBox, regionList);
		ImgPlus data = createCroppedData(boundingBox);

		LabelEditorPanel panel = new LabelEditorBdvPanel();
		panel.init(cropLabeling, data);
		JFrame frame = new JFrame("Label Detail Viewer");
		frame.setContentPane(panel.get());
		frame.setMinimumSize(new Dimension(500,500));
		frame.pack();
		frame.setVisible(true);

	}

	private <T extends NativeType<T>> ImgPlus createCroppedData(Interval boundingBox) {
		Img<T> dataImg = model.getData().factory().create(boundingBox);
		Cursor<T> dataInCursor = Views.zeroMin(Views.interval(model.getData(), boundingBox)).localizingCursor();
		RandomAccess<T> dataOutRA = dataImg.randomAccess();
		while(dataInCursor.hasNext()) {
			T val = dataInCursor.next();
			dataOutRA.setPosition(dataInCursor);
			dataOutRA.get().set(val);
		}
		return new ImgPlus(dataImg);
	}

	private ImgLabeling<L, IntType> createCroppedLabeling(Set<L> labels, Interval boundingBox, Map<L, LabelRegion<L>> regionList) {
		Img<IntType> backing = new ArrayImgFactory<>(new IntType()).create( boundingBox );
		ImgLabeling<L, IntType> cropLabeling = new ImgLabeling<>(backing);
		Point offset = new Point(boundingBox.numDimensions());
		for (int i = 0; i < boundingBox.numDimensions(); i++) {
			offset.setPosition(-boundingBox.min(i), i);
		}
		labels.forEach(label -> {
			LabelRegion<L> region = regionList.get(label);
			LabelRegionCursor inCursor = region.localizingCursor();
			RandomAccess<LabelingType<L>> outRA = cropLabeling.randomAccess();
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