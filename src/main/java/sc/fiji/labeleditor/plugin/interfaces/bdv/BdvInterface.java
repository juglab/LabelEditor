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
package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.cache.CacheControl;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.TimePointListener;
import bdv.viewer.ViewerPanel;
import bdv.viewer.state.SourceState;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.ui.PainterThread;
import net.imglib2.ui.TransformListener;
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
import sc.fiji.labeleditor.core.view.LabelEditorOverlayRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorRenderer;
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;
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

public class BdvInterface implements LabelEditorInterface, TimePointListener, TransformListener<AffineTransform3D> {

	@Parameter
	private Context context;

	private final BdvHandle bdvHandle;
	private final LabelEditorOverlay overlay = new LabelEditorOverlay();
	private boolean overlayAdded = false;

	private final Map<LabelEditorView<?>, List<BdvSource>> sources = new HashMap<>();
	private final Map<LabelEditorRenderer<?>, BdvSource> rendererSources = new HashMap<>();
	private final Map<InteractiveLabeling<?>, Behaviours> behavioursMap = new HashMap<>();
	private final Map<LabelEditorOverlayRenderer<?>, LabelEditorRenderingOverlay> rendererOverlays = new HashMap<>();
	private final PopupBehaviours popupBehaviours;
	private final BdvLabelingRenderer screenRenderer;

	public BdvInterface(BdvHandle bdvHandle, Context context) {
		this.bdvHandle = bdvHandle;
		this.context = context;
		popupBehaviours = new PopupBehaviours(this);
		if(context != null) context.inject(popupBehaviours);
		Behaviours behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor-popup");
		behaviours.install(this.bdvHandle.getTriggerbindings(), "labeleditor-popup");
		popupBehaviours.install(behaviours, bdvHandle.getViewerPanel());

		screenRenderer = new BdvLabelingRenderer(
				new PainterThread( null ),
				false,
				1,
				null,
				false,
				new CacheControl.Dummy() );
		bdvHandle.getViewerPanel().addTimePointListener(this);
		bdvHandle.getViewerPanel().addTransformListener(this);
		bdvHandle.getViewerPanel().addRenderTransformListener(this);
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, BdvHandle handle, Context context) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view, handle, context);
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BdvHandle bdvHandle, Context context) {
		BdvInterface interfaceInstance = new BdvInterface(bdvHandle, context);
		return interfaceInstance.control(model, view);
	}

	public <L> DefaultInteractiveLabeling<L> control(LabelEditorModel<L> model) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view);
	}

	public <L> DefaultInteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view) {
		return control(model, view, new BdvOptions());
	}

	public <L> DefaultInteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BdvOptions options) {
		DefaultInteractiveLabeling<L> interactiveLabeling = new DefaultInteractiveLabeling<>(model, view, this);
		if(context != null) context.inject(interactiveLabeling);
		interactiveLabeling.initialize();
		display(interactiveLabeling.model(), interactiveLabeling.view(), options);
		return interactiveLabeling;
	}

	public <L> void remove(InteractiveLabeling<L> labeling) {
		for (LabelEditorRenderer<L> renderer : labeling.view().renderers()) {
			rendererSources.remove(renderer);
		}
		List<BdvSource> toBeRemoved = sources.get(labeling.view());
		if(toBeRemoved != null) {
			sources.remove(labeling.view());
			for (BdvSource bdvSource : toBeRemoved) {
				bdvSource.removeFromBdv();
			}
		}
		Behaviours behaviours = behavioursMap.get(labeling);
		if(behaviours != null) {
			behaviours.getInputTriggerMap().clear();
			behaviours.getBehaviourMap().clear();
			//TODO are behaviours now properly removed from BDV?
			behavioursMap.remove(labeling);
		}
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
//		install(labeling, new FocusBehaviours<>(), behaviours);
		install(labeling, new LabelingModificationBehaviours<>(), behaviours);
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
		rendererSources.forEach((renderer, source) -> {
			if(source == null || renderer == null) return;
			source.setActive(renderer.isActive());
		});
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

	public <L> void display(LabelEditorModel<L> model, LabelEditorView<L> view, BdvOptions options) {
		ArrayList<BdvSource> sources = new ArrayList<>();
		sources.add(displayModelIndexImage(model));
		List<LabelEditorRenderer<L>> renderers = new ArrayList<>(view.renderers());
		Collections.reverse(renderers);
		renderers.forEach(renderer -> {
			if(LabelEditorOverlayRenderer.class.isAssignableFrom(renderer.getClass())) {
				displayAsOverlay(model, (LabelEditorOverlayRenderer<L>)renderer);
			} else {
				display(renderer, options, sources);
			}
		});
		this.sources.put(view, sources);
	}

	private <L> BdvStackSource<? extends IntegerType<?>> displayModelIndexImage(LabelEditorModel<L> model) {
		BdvStackSource<? extends IntegerType<?>> source = BdvFunctions.show(model.labeling().getIndexImg(), getModelIndexSourceName(model), BdvOptions.options().addTo(bdvHandle));
		source.setActive(false);
		return source;
	}

	private <L> void display(LabelEditorRenderer<L> renderer, BdvOptions options, ArrayList<BdvSource> sources) {
		BdvSource source = display(renderer.getOutput(), renderer.getName(), options);
		rendererSources.put(renderer, source);
		sources.add(source);
	}

	private <L> void displayAsOverlay(LabelEditorModel<L> model, LabelEditorOverlayRenderer<L> renderer) {

		LabelEditorRenderingOverlay renderingOverlay = new LabelEditorRenderingOverlay();
		renderer.init(model, this.screenRenderer.getScreenImage());
		renderer.updateOnTagChange();
		String rendererSourceName = model.getName() + "_" + renderer.getName();
		BdvFunctions.showOverlay(renderingOverlay, rendererSourceName, BdvOptions.options().addTo(bdvHandle));
		rendererOverlays.put(renderer, renderingOverlay);
		updateLabelRendering();
	}

	private <L> String getModelIndexSourceName(LabelEditorModel<L> model) {
		return model.getName() + "_index";
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

	@Override
	public void setRendererActive(LabelEditorRenderer renderer, boolean active) {
		BdvSource source = rendererSources.get(renderer);
		if(source != null) source.setActive(active);
	}

	@Override
	public Set<InteractiveLabeling<?>> getInteractiveLabelings() {
		return behavioursMap.keySet();
	}

	@Override
	public void timePointChanged(int timePointIndex) {
		updateLabelRendering();
	}

	@Override
	public void transformChanged(AffineTransform3D affineTransform3D) {
		updateLabelRendering();
	}

	private void updateLabelRendering() {
		screenRenderer.requestRepaint();
		int width = bdvHandle.getViewerPanel().getDisplay().getWidth();
		int height = bdvHandle.getViewerPanel().getDisplay().getHeight();
		if(width > 0 && height > 0) {
			rendererOverlays.forEach((renderer, overlay) -> {
				SourceAndConverter<?> thisSourceConverter = getSourceAndConverter(renderer);
				if(thisSourceConverter == null) return;
				screenRenderer.paint(
						width,
						height,
						bdvHandle.getViewerPanel().state(),
						thisSourceConverter);
				renderer.updateScreenImage(screenRenderer.getScreenImage());
				overlay.updateContent(renderer.getOutput());
			});
		}
	}

	private SourceAndConverter<?> getSourceAndConverter(LabelEditorOverlayRenderer<?> renderer) {
		List<SourceAndConverter<?>> sourcesList = bdvHandle.getViewerPanel().state().getSources();
		SourceAndConverter<?> thisSourceConverter = null;
		for (SourceAndConverter<?> sourceAndConverter : sourcesList) {
			if(sourceAndConverter.getSpimSource().getName().equals(getModelIndexSourceName(renderer.model()))) {
				thisSourceConverter = sourceAndConverter;
			}
		}
		return thisSourceConverter;
	}
}
