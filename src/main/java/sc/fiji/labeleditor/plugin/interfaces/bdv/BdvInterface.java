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

import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.BdvStackSource;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerPanel;
import bdv.viewer.render.AccumulateProjectorFactory;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.logic.BitType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.IntegerType;
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
import sc.fiji.labeleditor.core.view.LabelEditorView;
import sc.fiji.labeleditor.core.view.ViewChangedEvent;
import sc.fiji.labeleditor.plugin.behaviours.PopupBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.modification.LabelingModificationBehaviours;
import sc.fiji.labeleditor.plugin.behaviours.select.SelectionBehaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BdvInterface implements LabelEditorInterface {

	@Parameter
	private Context context;

	private BdvHandle bdvHandle;
	private final LabelEditorOverlay overlay = new LabelEditorOverlay();
	private boolean overlayAdded = false;

	private final List<BdvSource> dataSources = new ArrayList<>();
	private final Map<LabelEditorView<?>, List<BdvSource>> sources = new HashMap<>();
	private final Map<SourceAndConverter, InteractiveLabeling<?>> indexImgSources = new HashMap<>();
	private final Map<InteractiveLabeling<?>, Behaviours> behavioursMap = new HashMap<>();
	private PopupBehaviours popupBehaviours;
	private AccumulateProjectorFactory<ARGBType> factory;

	public BdvInterface(Context context) {
		this.context = context;
		factory = LabelEditorAccumulateProjector.createFactory(this);
	}

	public void setup(BdvHandle bdvHandle) {
		this.bdvHandle = bdvHandle;
		popupBehaviours = new PopupBehaviours(this);
		if(context != null) context.inject(popupBehaviours);
		Behaviours behaviours = new Behaviours(new InputTriggerConfig(), "labeleditor-popup");
		behaviours.install(this.bdvHandle.getTriggerbindings(), "labeleditor-popup");
		popupBehaviours.install(behaviours, bdvHandle.getViewerPanel());
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, BdvHandle handle, Context context) {
		LabelEditorView<L> view = new DefaultLabelEditorView<>(model);
		if(context != null) context.inject(view);
		view.addDefaultRenderers();
		return control(model, view, handle, context);
	}

	public static <L> InteractiveLabeling<L> control(LabelEditorModel<L> model, LabelEditorView<L> view, BdvHandle bdvHandle, Context context) {
		BdvInterface interfaceInstance = new BdvInterface(context);
		interfaceInstance.setup(bdvHandle);
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
		display(interactiveLabeling, options);
		return interactiveLabeling;
	}

	public <L> void remove(InteractiveLabeling<L> labeling) {
		List<BdvSource> toBeRemoved = sources.get(labeling.view());
		if(toBeRemoved != null) {
			sources.remove(labeling.view());
			for (BdvSource bdvSource : toBeRemoved) {
				dataSources.remove(bdvSource);
				bdvSource.removeFromBdv();
			}
		}
		for (Map.Entry<SourceAndConverter, InteractiveLabeling<?>> entry : indexImgSources.entrySet()) {
			SourceAndConverter bdvSource = entry.getKey();
			if(entry.getValue().equals(labeling)) {
				indexImgSources.remove(bdvSource);
				break;
			}
		}
		Behaviours behaviours = behavioursMap.get(labeling);
		if(behaviours != null) {
			behaviours.getInputTriggerMap().clear();
			behaviours.getBehaviourMap().clear();
			//TODO are behaviours now properly removed from BDV?
			behavioursMap.remove(labeling);
		}
		overlay.removeContent(labeling.model());
		bdvHandle.getViewerPanel().updateUI();
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

	public <L> void display(InteractiveLabeling<L> labeling, BdvOptions options) {
		ArrayList<BdvSource> sources = new ArrayList<>();
		BdvSource dataSource = displayModelData(labeling.model());
		if(dataSource != null) {
			dataSources.add(dataSource);
			sources.add(dataSource);
		}
		sources.add(displayModelIndexImage(labeling));
		this.sources.put(labeling.view(), sources);
	}

	private <L> BdvSource displayModelData(LabelEditorModel<L> model) {
		if(model.getData() != null) {
			return BdvFunctions.show(model.getData(), model.getName() + " raw", BdvOptions.options().addTo(bdvHandle));
		}
		return null;
	}

	private <L> BdvStackSource displayModelIndexImage(InteractiveLabeling<L> labeling) {
		RandomAccessibleInterval<ARGBType> indexImg = convertToARGB(labeling.model().labeling().getIndexImg());
		BdvStackSource<ARGBType> source = BdvFunctions.show(
				indexImg,
				getModelIndexSourceName(labeling.model()),
				BdvOptions.options().addTo(bdvHandle));
		indexImgSources.put(source.getSources().get(0), labeling);
		if(!overlayAdded) {
			overlayAdded = true;
			BdvFunctions.show(new ArrayImgFactory<>(new BitType()).create(10, 10), "dummy", BdvOptions.options().addTo(bdvHandle));
			BdvFunctions.showOverlay(overlay, "labeleditor", BdvOptions.options().addTo(bdvHandle));
		}
		return source;
	}

	private <T extends IntegerType<T>> RandomAccessibleInterval<ARGBType> convertToARGB(RandomAccessibleInterval<T> indexImg) {
		Converter<T, ARGBType> argbTypeConverter = (input, output) -> {
			output.set(input.getInteger());
		};
		return Converters.convert(indexImg, argbTypeConverter, new ARGBType());
	}

	private <L> String getModelIndexSourceName(LabelEditorModel<L> model) {
		return model.getName() + "_index";
	}

	@Override
	public Set<InteractiveLabeling<?>> getInteractiveLabelings() {
		return behavioursMap.keySet();
	}

	public AccumulateProjectorFactory<ARGBType> projector() {
		return factory;
	}

	Map<SourceAndConverter, InteractiveLabeling<?>> getIndexSources() {
		return indexImgSources;
	}

	List<BdvSource> getDataSources() {
		return dataSources;
	}
}
