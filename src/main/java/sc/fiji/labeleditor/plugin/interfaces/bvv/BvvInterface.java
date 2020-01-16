package sc.fiji.labeleditor.plugin.interfaces.bvv;

import bdv.util.BdvHandle;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvStackSource;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;
import sc.fiji.labeleditor.core.controller.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.controller.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.LabelEditorBehaviours;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.model.tagging.TagChangedEvent;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;
import sc.fiji.labeleditor.plugin.behaviours.FocusBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.PopupBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BvvInterface<L> implements LabelEditorInterface<L> {

	@Parameter
	private Context context;

	private final Behaviours behaviours;
	private BvvHandle bvvHandle;
	private final Map<LabelEditorView<L>, List<BvvStackSource>> bvvSources = new HashMap<>();
	private LabelingType<L> labelsAtCursor;

	public BvvInterface(BvvHandle handle) {
		this.bvvHandle = handle;
		this.behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor");
		behaviours.install(handle.getTriggerbindings(), "labeleditor");
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, BvvHandle handle, Context context) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view, handle, context);
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BvvHandle bvvHandle, Context context) {
		BvvInterface<L> interfaceInstance =  new BvvInterface<L>(bvvHandle);
		DefaultInteractiveLabeling<L> interactiveLabeling = new DefaultInteractiveLabeling<>(model, view, interfaceInstance);
		if(context != null) context.inject(interactiveLabeling);
		interactiveLabeling.initialize();
		return interactiveLabeling;
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
	public void display(LabelEditorView<L> view) {
		ArrayList<BvvStackSource> sources = new ArrayList<>();
		List<LabelEditorRenderer<L>> renderers = new ArrayList<>(view.renderers());
		Collections.reverse(renderers);
		renderers.forEach(renderer -> sources.add(display(renderer.getOutput(), renderer.getName())));
		bvvSources.put(view, sources);
	}

	private BvvStackSource display(final RandomAccessibleInterval img,
	                          final String title ) {
		final BvvStackSource source = BvvFunctions.show(
				img,
				title,
				Bvv.options().addTo(bvvHandle) );
		source.setActive( true );
		return source;
	}

	@Override
	public void installBehaviours(InteractiveLabeling<L> labeling) {
		install(labeling, new SelectionBehaviours<>());
		install(labeling, new FocusBehaviours<>());
		install(labeling, new LabelingModificationBehaviours<>());
		install(labeling, new PopupBehaviours<>());
	}

	@Override
	public void install(LabelEditorBehaviours<L> behaviour, InteractiveLabeling<L> labeling) {
		if(context != null) context.inject(behaviour);
		behaviour.init(labeling);
		behaviour.install(behaviours(), getComponent());
	}

	private void install(InteractiveLabeling<L> labeling, LabelEditorBehaviours<L> behavioursAdded) {
		if(context != null) context.inject(behavioursAdded);
		behavioursAdded.init(labeling);
		behavioursAdded.install(behaviours, bvvHandle.getViewerPanel().getDisplay());
	}

	@Override
	public void onViewChange(ViewChangedEvent viewChangedEvent) {
		bvvHandle.getViewerPanel().requestRepaint();
		List<BvvStackSource> sources = new ArrayList<>();
		bvvSources.forEach((lLabelEditorView, sources1) -> sources.addAll(sources1));
		sources.forEach(BvvStackSource::invalidate);
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
