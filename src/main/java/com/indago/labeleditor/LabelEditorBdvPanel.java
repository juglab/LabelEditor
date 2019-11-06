package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.BdvActionHandler;
import com.indago.labeleditor.action.InputTriggerConfig2D;
import com.indago.labeleditor.display.RenderingManager;
import com.indago.labeleditor.model.LabelEditorModel;
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
	}

	@Override
	public void init(ImgPlus data, LabelEditorModel<L> model) {
		super.init(data, model);
		bdvRemoveAll();
		addDataToBDV();
		addLabelsToBDV();
	}

	@Override
	public void init(ImgLabeling<L, IntType> labels) {
		super.init(labels);
		bdvRemoveAll();
		addLabelsToBDV();
	}

	@Override
	public void init(LabelEditorModel<L> model) {
		super.init(model);
		bdvRemoveAll();
		addLabelsToBDV();
	}

	@Override
	protected Component buildViewer() {
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		if(!mode3D && config != null ) {
			System.out.println("2D mode");
			bdvHandlePanel = new BdvHandlePanel( (Frame) this.getTopLevelAncestor(), Bdv.options().is2D().inputTriggerConfig(config));
		} else {
			System.out.println("3D mode");
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options() );
		}
		return bdvHandlePanel.getViewerPanel();
	}

	@Override
	protected ActionHandler<L> initActionHandler(LabelEditorModel<L> model, RenderingManager<L> renderer) {
		return new BdvActionHandler<>(bdvHandlePanel, model, renderer);
	}

	private void addLabelsToBDV() {
		if(rendering().size() == 0) return;
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
		//TODO display all renderings
		displayInBdv(rendering().getRenderings().get(0),"solution");
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
				Bdv.options().addTo( bdvGetHandlePanel() ) );
		bdvGetSources().add( source );
		source.setActive( true );
	}

	private void bdvRemoveAll() {
		for ( final BdvSource source : bdvGetSources()) {
			source.removeFromBdv();
		}
		bdvGetSources().clear();
	}

	public BdvHandlePanel bdvGetHandlePanel() {
		return bdvHandlePanel;
	}

	public List< BdvSource > bdvGetSources() {
		return bdvSources;
	}

	@Override
	public synchronized void updateLabelRendering() {
		rendering().update();
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	@Override
	public void updateData(ImgPlus<L> data) {
		super.setData(data);
		displayInBdv(data, "RAW");
		updateLabelRendering();
	}

	@Override
	public void dispose() {
		if(bdvGetHandlePanel() != null) bdvGetHandlePanel().close();
	}
}
