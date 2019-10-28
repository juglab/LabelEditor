package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.AbstractActionHandler;
import com.indago.labeleditor.action.BdvActionHandler;
import com.indago.labeleditor.action.InputTriggerConfig2D;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.LabelEditorModel;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.IntType;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBdvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();

	public LabelEditorBdvPanel() {
		super();
	}

	public LabelEditorBdvPanel(ImgPlus data) {
		super(data);
		populateBdv();
	}

	public LabelEditorBdvPanel(ImgLabeling<L, IntType> labels) {
		super(labels);
		populateBdv();
	}

	public LabelEditorBdvPanel(ImgPlus data, ImgLabeling<L, IntType> labels) {
		super(data, labels);
		populateBdv();
	}

	public LabelEditorBdvPanel(LabelEditorModel<L> model) {
		super(model);
		populateBdv();
	}

	public LabelEditorBdvPanel(ImgPlus data, LabelEditorModel<L> model) {
		super(data, model);
		populateBdv();
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
	protected ActionHandler<L> initActionHandler(LabelEditorModel<L> model, LabelEditorRenderer<L> renderer) {
		return new BdvActionHandler<>(bdvHandlePanel, model, renderer);
	}


	private void populateBdv() {
		bdvRemoveAll();
		if(data != null) {
			displayInBdv( data, "RAW" );
		}
		if(renderer == null) return;
		RandomAccessibleInterval<ARGBType> labelColorImg = renderer.getRenderedLabels();

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
		BdvFunctions.show(
				labelColorImg,
				"solution",
				Bdv.options().addTo(bdvGetHandlePanel()));
//		}
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
		renderer.update();
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

}
