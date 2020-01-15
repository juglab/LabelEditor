package sc.fiji.labeleditor.plugin.mode.timeslice;

import bdv.viewer.TimePointListener;
import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.roi.labeling.LabelingType;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.plugin.interfaces.bdv.BdvInterface;

import java.util.HashSet;
import java.util.Set;

public class TimeSliceLabelEditorController<L> extends DefaultLabelEditorController<L> {

	private long timePoint = 0;
	private Set<L> labelsInScope = new HashSet<>();
	private boolean processingLabelsInScope = false;

	public InteractiveLabeling init(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorInterface<L> interfaceInstance) {
		InteractiveLabeling res = super.init(model, view, interfaceInstance);
		try {
			BdvInterface bdv = (BdvInterface) interfaceInstance;
			bdv.getComponent().addTimePointListener(this::timePointChanged);
		} catch (ClassCastException e) {
			System.err.println("Cannot add a timepoint listener to interface " + interfaceInstance.getClass().getName());
		}
		return res;
	}

	private void timePointChanged(int index) {
		this.timePoint = index;
		for (LabelEditorRenderer renderer : view.renderers()) {
			if(renderer instanceof TimePointListener) {
				((TimePointListener) renderer).timePointChanged(index);
			}
		}
		view.updateRenderers();
		new Thread(() -> {
			processingLabelsInScope = true;
			labelsInScope.clear();
			boolean[] setDone = new boolean[model.labeling().getMapping().numSets()];
			Cursor<LabelingType<L>> cursor = labelingInScope().cursor();
			while(cursor.hasNext()) {
				int val = cursor.next().getIndex().getInteger();
				if(setDone[val]) continue;
				setDone[val] = true;
				Set<L> labels = cursor.get();
				if(labels.size() == 0) continue;
				labelsInScope.addAll(labels);
			}
			processingLabelsInScope = false;
		}).start();
	}

	@Override
	public IterableInterval<LabelingType<L>> labelingInScope() {
		try {
			return ((TimeSliceLabelEditorModel<L>) model).getLabelingAtTime(timePoint);
		} catch (ClassCastException e) {
			System.err.println("Model is no TimeSliceLabelEditorModel. Operation will be performed on the whole labeling instead of only one timepoint.");
		}
		return model.labeling();
	}

	@Override
	public Set<L> labelSetInScope() {
		while(processingLabelsInScope){}
		return labelsInScope;
	}
}
