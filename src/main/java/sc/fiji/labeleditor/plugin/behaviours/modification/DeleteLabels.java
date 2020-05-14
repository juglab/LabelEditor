package sc.fiji.labeleditor.plugin.behaviours.modification;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.LabelRegions;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.view.Views;
import org.scijava.ui.behaviour.Behaviour;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.model.tagging.LabelEditorTag;

import java.util.Set;

public class DeleteLabels<L> implements Behaviour {

	private final InteractiveLabeling<L> labeling;

	public DeleteLabels(InteractiveLabeling<L> labeling) {
		this.labeling = labeling;
	}

	public void deleteSelected() {
		Set<L> selected = labeling.model().tagging().getLabels(LabelEditorTag.SELECTED);
		delete(selected, labeling.getLabelingInScope());
		labeling.view().updateOnLabelingChange();
	}

	private static <L> void delete(Set<L> labels, RandomAccessibleInterval<LabelingType<L>> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		for (L label : labels) {
			IterableInterval<LabelingType<L>> sample = Regions.sample(regions.getLabelRegion(label), labeling);
			sample.forEach(pixel -> pixel.remove(label));
		}
	}

	static <L> void delete(L label, RandomAccessibleInterval<LabelingType<L>> labeling) {
		LabelRegions<L> regions = new LabelRegions<>(labeling);
		IterableInterval<LabelingType<L>> sample = Regions.sample(regions.getLabelRegion(label), labeling);
		sample.forEach(pixel -> pixel.remove(label));
	}
}
