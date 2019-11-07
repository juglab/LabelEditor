package com.indago.labeleditor;

import bdv.util.BdvHandlePanel;
import bvv.util.Bvv;
import bvv.util.BvvFunctions;
import bvv.util.BvvHandle;
import bvv.util.BvvSource;
import bvv.util.BvvStackSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.BvvActionHandler;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.DiskCachedCellImgFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import tpietzsch.example2.VolumeViewerPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBvvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BvvHandle bvvHandle;
	private List< BvvStackSource > bvvSources;

	@Override
	public void init(ImgPlus data) {
		super.init(data);
		bvvRemoveAll();
		addDataToBvv();
	}

	@Override
	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		super.init(data, labels);
		bvvRemoveAll();
		addDataToBvv();
		addLabelsToBvv();
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		super.init(data, model);
		bvvRemoveAll();
		addDataToBvv();
		addLabelsToBvv();
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		super.init(labels);
		bvvRemoveAll();
		addLabelsToBvv();
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		bvvRemoveAll();
		addLabelsToBvv();
	}

	@Override
	protected Component buildViewer() {
		bvvSources = new ArrayList<>();
		BvvStackSource<ARGBType> source1 = BvvFunctions.show(fakeImg(), "", Bvv.options());
		bvvSources.add(source1);
		bvvHandle = source1.getBvvHandle();
		return bvvHandle.getViewerPanel();
	}

	private ImgPlus<ARGBType> fakeImg() {
		return new ImgPlus<>(new DiskCachedCellImgFactory<>(new ARGBType()).create(model.labels()));
	}

	@Override
	protected ActionHandler<L> initActionHandler(LabelEditorModel<L> model, RenderingManager<L> renderer) {
		return new BvvActionHandler<>(getBvvHandle(), model, renderer);
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

	public BvvHandle getBvvHandle() {
		return bvvHandle;
	}

	public List< BvvStackSource > bvvGetSources() {
		return bvvSources;
	}

	@Override
	public synchronized void updateLabelRendering() {
		if(rendering().size() > 0) rendering().update();
		bvvHandle.getViewerPanel().requestRepaint();
		bvvSources.forEach(BvvStackSource::invalidate);
	}

	@Override
	public void dispose() {
		if(getBvvHandle() != null) getBvvHandle().close();
	}

	@Override
	public void updateData(ImgPlus<L> imgPlus) {
		super.setData(imgPlus);
	}

}
