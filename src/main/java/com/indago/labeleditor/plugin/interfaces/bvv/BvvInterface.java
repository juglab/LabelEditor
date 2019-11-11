package com.indago.labeleditor.plugin.interfaces.bvv;

import bvv.util.BvvHandle;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.core.model.DefaultLabelEditorModel;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BvvInterface<L> implements LabelEditorInterface<L> {
	private BvvHandle bvvHandle;
	private List<BvvStackSource> bvvSources;

	public BvvInterface(BvvHandle handle, List<BvvStackSource> sources) {
		this.bvvHandle = handle;
		this.bvvSources = sources;
	}

	public static <L> LabelEditorController<L> control(DefaultLabelEditorModel<L> model, LabelEditorView<L> view, BvvHandle bvvHandle, List<BvvStackSource> sources) {
		LabelEditorController<L> actionHandler = new LabelEditorController<>();
		actionHandler.init(model, view, new BvvInterface<L>(bvvHandle, sources));
		actionHandler.addDefaultBehaviours();
		return actionHandler;
	}

	protected List<LabelingType<L>> getAllLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV);
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition(MouseEvent e, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(e, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV).get(0);
	}

	private Set<LabelingType<L>> getLabelsAtMousePositionInBVV(MouseEvent e, LabelEditorModel<L> model) {
		RealPoint gPos = new RealPoint(3);
		assert gPos.numDimensions() == 3;
		final RealPoint lPos = new RealPoint( 3 );
		lPos.setPosition(e.getX(), 0);
		lPos.setPosition(e.getY(), 1);
		AffineTransform3D transform = new AffineTransform3D();

		int time = bvvHandle.getViewerPanel().getState().getCurrentTimepoint();
		Set<LabelingType<L>> labels = new HashSet<>();
		for (int i = 0; i < 500; i++) {
			lPos.setPosition(i, 2);
			bvvHandle.getViewerPanel().getState().getViewerTransform(transform);
			transform.applyInverse( gPos, lPos );
			final int x = ( int ) gPos.getFloatPosition( 0 );
			final int y = ( int ) gPos.getFloatPosition( 1 );
			final int z = ( int ) gPos.getFloatPosition( 2 );
			Point pos = new Point(x, y, z, time);
			try {
				LabelingType<L> labelsAtPosition = getLabelsAtPosition(pos, model);
				if(labelsAtPosition != null && labelsAtPosition.size() > 0) {
					labels.add(labelsAtPosition);
				}
			} catch(ArrayIndexOutOfBoundsException ignored) {}
		}
		return labels;
	}

	private LabelingType<L> getLabelsAtPosition(Localizable pos, LabelEditorModel<L> model) {
		RandomAccess<LabelingType<L>> ra = model.labels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
	}

	@Override
	public List<LabelEditorBehaviours> getAvailableActions(LabelEditorModel<L> model, LabelEditorController<L> controller) {
		List<LabelEditorBehaviours> res = new ArrayList<>();
		//TODO find actions by annotation
		res.add(new BvvSelectionBehaviours<>(model, controller, this));
		return res;
	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		bvvHandle.getViewerPanel().requestRepaint();
		bvvSources.forEach(BvvStackSource::invalidate);
	}

	public BvvHandle getBvvHandle() {
		return bvvHandle;
	}
}
