package com.indago.labeleditor.plugin.interfaces.bvv;

import bvv.util.BvvHandle;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.controller.DefaultLabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorBehaviours;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.core.model.LabelEditorModel;
import com.indago.labeleditor.core.model.tagging.TagChangedEvent;
import com.indago.labeleditor.core.view.LabelEditorView;
import com.indago.labeleditor.core.view.ViewChangedEvent;
import com.indago.labeleditor.plugin.behaviours.FocusBehaviours;
import com.indago.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import com.indago.labeleditor.plugin.behaviours.select.SelectionBehaviours;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BvvInterface<L> implements LabelEditorInterface<L> {
	private final Behaviours behaviours;
	private BvvHandle bvvHandle;
	private List<BvvStackSource> bvvSources;
	private LabelingType<L> labelsAtCursor;

	public BvvInterface(BvvHandle handle, List<BvvStackSource> sources) {
		this.bvvHandle = handle;
		this.bvvSources = sources;
		this.behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor");
		behaviours.install(handle.getTriggerbindings(), "labeleditor");
	}

	public static <L> LabelEditorController<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BvvHandle bvvHandle, List<BvvStackSource> sources) {
		LabelEditorController<L> actionHandler = new DefaultLabelEditorController<>();
		actionHandler.init(model, view, new BvvInterface<L>(bvvHandle, sources));
		actionHandler.addDefaultBehaviours();
		return actionHandler;
	}

	protected List<LabelingType<L>> getAllLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(x, y, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		return new ArrayList<>(labelsAtMousePositionInBVV);
	}

	@Override
	public LabelingType<L> findLabelsAtMousePosition(int x, int y, LabelEditorModel<L> model) {
		Set<LabelingType<L>> labelsAtMousePositionInBVV = getLabelsAtMousePositionInBVV(x, y, model);
		if(labelsAtMousePositionInBVV.size() == 0) return null;
		labelsAtCursor = new ArrayList<>(labelsAtMousePositionInBVV).get(0);
		return labelsAtCursor;
	}

	@Override
	public LabelingType<L> getLabelsAtMousePosition() {
		return labelsAtCursor;
	}

	private Set<LabelingType<L>> getLabelsAtMousePositionInBVV(int mx, int my, LabelEditorModel<L> model) {
		RealPoint gPos = new RealPoint(3);
		assert gPos.numDimensions() == 3;
		final RealPoint lPos = new RealPoint( 3 );
		lPos.setPosition(mx, 0);
		lPos.setPosition(my, 1);
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
		RandomAccess<LabelingType<L>> ra = model.labeling().randomAccess();
		ra.setPosition(pos);
		return ra.get();
	}

	@Override
	public void set3DViewMode(boolean mode3D) {
	}

	@Override
	public void installBehaviours(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView<L> view) {
		install(model, controller, view, new SelectionBehaviours<>());
		install(model, controller, view, new FocusBehaviours<>());
		install(model, controller, view, new LabelingModificationBehaviours());
	}

	private void install(LabelEditorModel<L> model, LabelEditorController<L> controller, LabelEditorView view, LabelEditorBehaviours behavioursAdded) {
		behavioursAdded.init(model, controller, view);
		behavioursAdded.install(behaviours, bvvHandle.getViewerPanel().getDisplay());
	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		bvvHandle.getViewerPanel().requestRepaint();
		bvvSources.forEach(BvvStackSource::invalidate);
	}

	@Override
	public Behaviours behaviours() {
		return behaviours;
	}

	public BvvHandle getBvvHandle() {
		return bvvHandle;
	}

	@Override
	public Component getComponent() {
		return bvvHandle.getViewerPanel();
	}

	@Override
	public void onTagChange(List<TagChangedEvent> tagChangedEvents) {
	}
}
