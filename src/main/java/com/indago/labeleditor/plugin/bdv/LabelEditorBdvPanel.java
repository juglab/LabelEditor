package com.indago.labeleditor.plugin.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import com.indago.labeleditor.core.AbstractLabelEditorPanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.core.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBdvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private LabelEditorInterface<L> viewerInstance;

	@Override
	public void init(ImgPlus data) {
		super.init(data);
		bdvRemoveAll();
		addDataToBDV();
	}

	@Override
	public void init(ImgPlus data, ImgLabeling<L, IntType> labels) {
		super.init(data, labels);
		bdvRemoveAll();
		addDataToBDV();
		addLabelsToBDV();
		initActionHandling();
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		super.init(data, model);
		bdvRemoveAll();
		addDataToBDV();
		addLabelsToBDV();
		initActionHandling();
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		super.init(labels);
		bdvRemoveAll();
		addLabelsToBDV();
		initActionHandling();
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		bdvRemoveAll();
		addLabelsToBDV();
		initActionHandling();
	}

	private void initActionHandling() {
		viewerInstance = new BdvInterface<>(bdvHandlePanel, bdvSources);
		initActionManager(actionManager);
		actionManager.set3DViewMode(mode3D);
	}

	@Override
	protected Component buildViewer() {
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		BdvOptions options = Bdv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		if(!mode3D && config != null ) {
			System.out.println("2D mode");
			bdvHandlePanel = new BdvHandlePanel( (Frame) this.getTopLevelAncestor(), options.is2D().inputTriggerConfig(config));
		} else {
			System.out.println("3D mode");
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), options);
		}
		return bdvHandlePanel.getViewerPanel();
	}

	@Override
	protected void initActionManager(LabelEditorController<L> actionManager) {
		actionManager.init(viewerInstance, model(), view());
		actionManager.addDefaultActionHandlers();
	}

	private void addLabelsToBDV() {
		if(view().size() == 0) return;
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
		view().getNamedRenderings().forEach((title, img) -> displayInBdv(img, title));
//		}
	}

	private void addDataToBDV() {
		if(data != null) {
			displayInBdv( data, "RAW" );
		}
	}

	private void displayInBdv( final RandomAccessibleInterval img,
	                           final String title ) {
		final BdvSource source = BdvFunctions.show(
				img,
				title,
				Bdv.options().addTo( getViewerHandle() ) );
		bdvGetSources().add( source );
		source.setActive( true );
	}

	private void bdvRemoveAll() {
		for ( final BdvSource source : bdvGetSources()) {
			source.removeFromBdv();
		}
		bdvGetSources().clear();
	}

	@Override
	public BdvHandlePanel getViewerHandle() {
		return bdvHandlePanel;
	}

	public List< BdvSource > bdvGetSources() {
		return bdvSources;
	}

	@Override
	public void updateData(ImgPlus<L> data) {
		super.setData(data);
		displayInBdv(data, "RAW");
		control().triggerTagChange();
	}

	@Override
	public void dispose() {
		if(getViewerHandle() != null) getViewerHandle().close();
	}
}
