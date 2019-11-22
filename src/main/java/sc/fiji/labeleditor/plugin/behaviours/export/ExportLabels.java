package sc.fiji.labeleditor.plugin.behaviours.export;

import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import sc.fiji.labeleditor.core.LabelEditorPanel;
import sc.fiji.labeleditor.core.model.DefaultLabelEditorModel;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;
import sc.fiji.labeleditor.plugin.interfaces.bdv.LabelEditorBdvPanel;
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
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.ui.behaviour.Behaviour;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExportLabels<L> implements Behaviour {

	@Parameter
	Context context;

	@Parameter
	UIService ui;

	@Parameter
	OpService ops;

	private final LabelEditorModel<L> model;

	public ExportLabels(LabelEditorModel<L> model) {
		this.model = model;
	}

	public void exportSelected() {
		Set<L> selected = model.tagging().getLabels(LabelEditorTag.SELECTED);

		LabelRegions regions = new LabelRegions<>(model.labeling());
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

		ImgLabeling<L, IntType> cropLabeling = createCroppedLabeling(selected, boundingBox, regionList);

		LabelEditorPanel panel = new LabelEditorBdvPanel();
		if(context != null) context.inject(panel);

		LabelEditorModel model;
		if(this.model.getData() != null) {
			RandomAccessibleInterval data = createCroppedData(boundingBox);
			model = new DefaultLabelEditorModel<>(cropLabeling, data);
		} else {
			model = new DefaultLabelEditorModel<>(cropLabeling);
		}

		ui.show(model);

	}

	private RandomAccessibleInterval createCroppedData(Interval boundingBox) {
		return ops.copy().rai(Views.interval(model.getData(), boundingBox));
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