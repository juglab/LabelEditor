package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.ViewerPanel;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.util.Intervals;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BdvInterface implements LabelEditorInterface {

	@Parameter
	private Context context;

	private final BdvHandle bdvHandle;
	private final LabelEditorOverlay overlay = new LabelEditorOverlay();
	private boolean overlayAdded = false;

	private final Map<LabelEditorView<?>, List<BdvSource>> sources = new HashMap<>();
	private final Map<InteractiveLabeling<?>, Behaviours> behavioursMap = new HashMap<>();
	private final PopupBehaviours popupBehaviours;

	public BdvInterface(BdvHandle bdvHandle, Context context) {
		this.bdvHandle = bdvHandle;
		this.context = context;
		popupBehaviours = new PopupBehaviours();
		context.inject(popupBehaviours);
		Behaviours behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor-popup");
		behaviours.install(this.bdvHandle.getTriggerbindings(), "labeleditor-popup");
		popupBehaviours.install(behaviours, bdvHandle.getViewerPanel());
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BdvHandle bdvHandle, Context context) {
		BdvInterface interfaceInstance = new BdvInterface(bdvHandle, context);
		DefaultInteractiveLabeling<L> interactiveLabeling = new DefaultInteractiveLabeling<>(model, view, interfaceInstance);
		if(context != null) context.inject(interactiveLabeling);
		interactiveLabeling.initialize();
		interfaceInstance.display(view, new BdvOptions());
		return interactiveLabeling;
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, BdvHandle handle, Context context) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view, handle, context);
	}

	public <L> DefaultInteractiveLabeling<L> control(LabelEditorModel<L> model) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view);
	}

	public <L> DefaultInteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view) {
		DefaultInteractiveLabeling<L> interactiveLabeling = new DefaultInteractiveLabeling<>(model, view, this);
		if(context != null) context.inject(interactiveLabeling);
		interactiveLabeling.initialize();
		display(view, new BdvOptions());
		return interactiveLabeling;
	}

	public <L> LabelingType<L> findLabelsAtMousePosition(int x, int y, InteractiveLabeling<L> labeling) {
		RandomAccess<LabelingType<L>> ra = labeling.getLabelingInScope().randomAccess();
		Localizable pos = getDataPositionAtMouse();
		if(Intervals.contains(labeling.getLabelingInScope(), pos)) {
			ra.setPosition(pos);
			return ra.get();
		}
		return null;
	}

	private Localizable getDataPositionAtMouse() {
		//FIXME currently only works for 2D, 3D and 4D
		RealPoint mousePointer = new RealPoint(3);
		bdvHandle.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
		final int x = (int) Math.round( mousePointer.getDoublePosition( 0 ) );
		final int y = (int) Math.round( mousePointer.getDoublePosition( 1 ) );
		final int z = (int) Math.round( mousePointer.getDoublePosition( 2 ) );
		int time = bdvHandle.getViewerPanel().getState().getCurrentTimepoint();
		return new Point(x, y, z, time);
	}

	@Override
	public <L> void installBehaviours(InteractiveLabeling<L> labeling) {
		SelectionBehaviours<L> selectionModel = new SelectionBehaviours<>();
		labeling.setSelectionModel(selectionModel);
		Behaviours behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor");
		behaviours.install(this.bdvHandle.getTriggerbindings(), "labeleditor" + labeling.toString());
		behavioursMap.put(labeling, behaviours);
		install(labeling, selectionModel, behaviours);
		install(labeling, new FocusBehaviours<>(), behaviours);
		install(labeling, new LabelingModificationBehaviours<>(), behaviours);
		popupBehaviours.add(labeling);
	}

	@Override
	public <L> Behaviours behaviours(InteractiveLabeling<L> labeling) {
		return behavioursMap.get(labeling);
	}

	private <L> void install(InteractiveLabeling<L> labeling, LabelEditorBehaviours<L> behavioursAdded, Behaviours behaviours) {
		if(context != null) context.inject(behavioursAdded);
		behavioursAdded.init(labeling);
		behavioursAdded.install(behaviours, bdvHandle.getViewerPanel().getDisplay());
	}

	@Override
	public <L> void install(LabelEditorBehaviours<L> behaviour, InteractiveLabeling<L> labeling) {
		if(context != null) context.inject(behaviour);
		behaviour.init(labeling);
		behaviour.install(behavioursMap.get(labeling), getComponent());
	}

	@Override
	public synchronized void onViewChange(ViewChangedEvent viewChangedEvent) {
		bdvHandle.getViewerPanel().requestRepaint();
	}

	@Override
	public ViewerPanel getComponent() {
		return bdvHandle.getViewerPanel();
	}

	@Override
	public synchronized void onTagChange(List<TagChangedEvent> tagChangedEvents) {
		Set<LabelEditorModel> models = new HashSet<>();
		for (TagChangedEvent tagChangedEvent : tagChangedEvents) {
			models.add(tagChangedEvent.model);
		}
		overlay.updateContent(models);
	}

	@Override
	public <L> void display(LabelEditorView<L> view) {
		ArrayList<BdvSource> sources = new ArrayList<>();
		List<LabelEditorRenderer<L>> renderers = new ArrayList<>(view.renderers());
		Collections.reverse(renderers);
		renderers.forEach(renderer -> sources.add(display(renderer.getOutput(), renderer.getName(), new BdvOptions())));
		this.sources.put(view, sources);
	}

	public <L> void display(LabelEditorView<L> view, BdvOptions options) {
		ArrayList<BdvSource> sources = new ArrayList<>();
		List<LabelEditorRenderer<L>> renderers = new ArrayList<>(view.renderers());
		Collections.reverse(renderers);
		renderers.forEach(renderer -> sources.add(display(renderer.getOutput(), renderer.getName(), options)));
		this.sources.put(view, sources);
	}

	private BdvSource display(RandomAccessibleInterval rai, String name, BdvOptions options) {
		if(rai == null) return null;
		final BdvSource source = BdvFunctions.show(rai, name, options.addTo(bdvHandle));
		source.setActive(true);
		if(!overlayAdded) {
			overlayAdded = true;
			BdvFunctions.showOverlay(overlay, "labeleditor", options.addTo(bdvHandle));
		}
		return source;
	}

	public Map<LabelEditorView<?>, List<BdvSource>> getSources() {
		return sources;
	}
}
