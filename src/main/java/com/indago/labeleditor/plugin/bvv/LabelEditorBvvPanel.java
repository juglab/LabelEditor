package com.indago.labeleditor.plugin.bvv;

import bdv.util.Bdv;
import bdv.util.BdvOptions;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvOptions;
import bvv.util.BvvSource;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.core.AbstractLabelEditorPanel;
import com.indago.labeleditor.core.action.ActionManager;
import com.indago.labeleditor.core.action.ViewerInstance;
import com.indago.labeleditor.core.display.LabelEditorAccumulateProjector;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBvvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BvvHandle bvvHandle;
	private List< BvvStackSource > bvvSources;
	private ViewerInstance<L> viewerInstance;

	@Override
	public void init(ImgPlus data) {
		super.init(data);
		bvvRemoveAll();
		addDataToBvv();
		initViewer();
	}

	@Override
	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		super.init(data, labels);
		bvvRemoveAll();
		addDataToBvv();
		addLabelsToBvv();
		initViewer();
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		super.init(data, model);
		bvvRemoveAll();
		addDataToBvv();
		addLabelsToBvv();
		initViewer();
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		super.init(labels);
		bvvRemoveAll();
		addLabelsToBvv();
		initViewer();
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		bvvRemoveAll();
		addLabelsToBvv();
		initViewer();
	}

	private void initViewer() {
		viewerInstance = new BvvViewerInstance<>(bvvHandle, bvvSources);
		initActionManager(actionManager);
		actionManager.set3DViewMode(mode3D);
	}

	@Override
	public ViewerInstance<L> viewer() {
		return viewerInstance;
	}

	@Override
	protected Component buildViewer() {
		bvvSources = new ArrayList<>();
//		BvvOptions options = Bvv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		BvvStackSource<ARGBType> source1 = BvvFunctions.show(fakeImg(), "", Bvv.options());
		bvvSources.add(source1);
		bvvHandle = source1.getBvvHandle();
		return bvvHandle.getViewerPanel();
	}

	private ImgPlus<ARGBType> fakeImg() {
		return new ImgPlus<>(new DiskCachedCellImgFactory<>(new ARGBType()).create(model.labels()));
	}

	private void addLabelsToBvv() {
		if(rendering().size() == 0) return;
		//TODO add all renderers
		RandomAccessibleInterval<ARGBType> labelColorImg = rendering().getRenderings().get(0);

		//TODO make virtual channels work
//		List<LUTChannel> virtualChannels = renderer.getVirtualChannels();
//		if(virtualChannels != null) {
//			List<BdvVirtualChannelSource> sources = BdvFunctions.show(
//					labelColorImg,
//					virtualChannels,
//					"solution",
//					Bdv.options().addTo(bdvGetHandlePanel()));
//			final Bdv bdv = sources.get( 0 );
//			for (int i = 0; i < virtualChannels.size(); ++i ) {
//				virtualChannels.get( i ).setPlaceHolderOverlayInfo( sources.get( i ).getPlaceHolderOverlayInfo() );
//				virtualChannels.get( i ).setViewerPanel( bdv.getBdvHandle().getViewerPanel() );
//			}
//		} else {
		displayInBvv(labelColorImg, "solution");
//		}
	}

	private void addDataToBvv() {
		if(data != null) {
			displayInBvv( data, "RAW" );
		}
	}

	private void displayInBvv(final RandomAccessibleInterval img,
	                          final String title ) {
		final BvvStackSource source = BvvFunctions.show(
				img,
				title,
				Bvv.options().addTo(bvvHandle) );
		bvvGetSources().add( source );
		source.setActive( true );
	}

	private void bvvRemoveAll() {
		for ( final BvvSource source : bvvGetSources()) {
			source.removeFromBdv();
		}
		bvvGetSources().clear();
	}

	@Override
	public BvvHandle getViewerHandle() {
		return bvvHandle;
	}

	public List< BvvStackSource > bvvGetSources() {
		return bvvSources;
	}

	@Override
	protected void initActionManager(ActionManager<L> actionManager) {
		actionManager.init(viewer(), model(), rendering());
		actionManager.addDefaultActionHandlers();
	}

//	public synchronized void updateLabelRendering() {
//		//FIXME figure out how to call this from ActionManager::updateLabelRendering
//		rendering().update();
//		bvvHandle.getViewerPanel().requestRepaint();
//		bvvSources.forEach(BvvStackSource::invalidate);
//	}

	@Override
	public void dispose() {
		if(getViewerHandle() != null) getViewerHandle().close();
	}

	@Override
	public void updateData(ImgPlus<L> imgPlus) {
		super.setData(imgPlus);
	}

}
