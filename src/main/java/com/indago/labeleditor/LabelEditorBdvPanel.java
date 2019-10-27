package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.DefaultActionHandler;
import com.indago.labeleditor.action.InputTriggerConfig2D;
import com.indago.labeleditor.display.DefaultLabelEditorRenderer;
import com.indago.labeleditor.display.LabelEditorRenderer;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.util.ImgLib2Util;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBdvPanel<L, T extends RealType<T>> extends JPanel {

	private ImgPlus<T> data;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();

	private LabelEditorModel<L> model;
	private LabelEditorRenderer<L> renderer;
	private ActionHandler<L> actionHandler;

	private boolean panelBuilt = false;
	private boolean mode3D = false;

	public LabelEditorBdvPanel() {
	}

	public LabelEditorBdvPanel(ImgPlus<T> data) {
		setData(data);
		buildPanel();
	}

	public LabelEditorBdvPanel(ImgLabeling<L, IntType > labels) {
		init(labels);
	}

	public LabelEditorBdvPanel(ImgPlus< T > data, ImgLabeling<L, IntType > labels) {
		init(data, labels);
	}

	public LabelEditorBdvPanel(LabelEditorModel<L> model) {
		init(model);
	}

	public LabelEditorBdvPanel(ImgPlus<T> data, LabelEditorModel<L> model) {
		setData(data);
		init(model);
	}

	public void init(ImgPlus<T> data, ImgLabeling<L, IntType> labels) {
		setData(data);
		init(labels);
	}

	public void init(ImgLabeling<L, IntType> labels) {
		init(new DefaultLabelEditorModel<>(labels));
	}

	public void init(LabelEditorModel<L> model) {
		if(model != null) {
			this.model = model;
			actionHandler = initActionHandler(model);
			renderer = initRenderer(model);
		}
		buildPanel();
	}

	private void setData(ImgPlus<T> data) {
		this.data = data;
		if(data.dimensionIndex(Axes.Z) > 0) {
			mode3D = true;
		}
	}

	private void buildPanel() {
		if(panelBuilt) return;
		panelBuilt = true;
		//this limits the BDV navigation to 2D
		setLayout( new BorderLayout() );
		final JPanel viewer = new JPanel( new MigLayout("fill, w 500, h 500") );
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		if(!mode3D && config != null ) {
			System.out.println("2D mode");
			actionHandler.set3DViewMode(false);
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options().is2D().inputTriggerConfig(config));
		} else {
			System.out.println("3D mode");
			actionHandler.set3DViewMode(true);
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options() );
		}

		// TODO... How do you find out what kind of data it is? A utility perhaps or is it good enough to check that config is not null? Can it be null in a generic case?
		viewer.add( bdvHandlePanel.getViewerPanel(), "span, grow, push" );
		this.add( viewer );
		populateBdv();
		if(actionHandler != null) actionHandler.init();
	}

	protected ActionHandler<L> initActionHandler(LabelEditorModel<L> model) {
		return new DefaultActionHandler<L>(this, model);
	}

	protected LabelEditorRenderer<L> initRenderer(LabelEditorModel<L> model) {
		return new DefaultLabelEditorRenderer<L>(model);
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

	private void displayInBdv( final RandomAccessibleInterval< T > img,
			final String title ) {
		final BdvSource source = BdvFunctions.show(
				img,
				title,
				Bdv.options().addTo( bdvGetHandlePanel() ) );
		bdvGetSources().add( source );

		ValuePair<T, T> minMax = ImgLib2Util.computeMinMax(Views.iterable(img));
		if(!minMax.getA().equals(minMax.getB())) {
			source.setDisplayRangeBounds( Math.min( minMax.getA().getRealDouble(), 0 ), minMax.getB().getRealDouble() );
			source.setDisplayRange( minMax.getA().getRealDouble(), minMax.getB().getRealDouble() );
		}
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

	public synchronized void updateLabelRendering() {
		renderer.update();
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	public LabelEditorRenderer<L> getRenderer() {
		return renderer;
	}

	public LabelEditorModel<L> getModel() {
		return model;
	}

	public ActionHandler<L> getActionHandler() {
		return actionHandler;
	}
}
