package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.action.ActionHandler;
import com.indago.labeleditor.action.DefaultActionHandler;
import com.indago.labeleditor.action.InputTriggerConfig2D;
import com.indago.labeleditor.display.DefaultLUTBuilder;
import com.indago.labeleditor.display.LUTBuilder;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.util.ImgLib2Util;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
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
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorPanel<T extends RealType<T>, U> extends JPanel {

	private LabelEditorModel<U> model;
	private ImgPlus<T> data;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private MouseMotionListener mml;

	private LUTBuilder<U> lutBuilder;
	private ActionHandler actionHandler;
	private int[] lut;

	public LabelEditorPanel() {
		init(null);
	}

	public LabelEditorPanel( ImgPlus<T> data) {
		this(new DefaultLabelEditorModel<>());
		this.data = data;
	}

	public LabelEditorPanel( ImgPlus< T > data, ImgLabeling< U, IntType > labels) {
		this( new DefaultLabelEditorModel<>( labels ) );
		this.data = data;
	}

	public LabelEditorPanel(LabelEditorModel<U> model) {
		init(model);
	}

	public LabelEditorPanel( ImgPlus<T> data, LabelEditorModel model) {
		this.data = data;
		init(model);
	}

	public void init(ImgPlus<T> data, ImgLabeling<U, IntType> labels) {
		init(new DefaultLabelEditorModel<>(labels));
		this.data = data;
	}

	public void init(LabelEditorModel<U> model) {

		setLayout( new BorderLayout() );

		lutBuilder = initLUTBuilder();
		actionHandler = initActionHandler();

		if(model != null) buildPanelFromModel(model);

	}

	private void buildPanelFromModel(LabelEditorModel<U> model) {
		this.model = model;
		//this limits the BDV navigation to 2D
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		buildGui(config);
		populateBdv();
		actionHandler.init();
	}

	public void updateLUT() {
		lut = lutBuilder.build(model);
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	protected LUTBuilder<U> initLUTBuilder() {
		return new DefaultLUTBuilder<>();
	}

	private ActionHandler initActionHandler() {
		return new DefaultActionHandler<U>(this);
	}

	private void buildGui(InputTriggerConfig config) {
		final JPanel viewer = new JPanel( new MigLayout("fill, w 500, h 500") );

		// TODO... How do you find out what kind of data it is? A utility perhaps or is it good enough to check that config is not null? Can it be null in a generic case?
		if ( /*data.is2D()*/ config != null ) {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options().is2D().inputTriggerConfig(config));
		} else {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options() );
		}
		viewer.add( bdvHandlePanel.getViewerPanel(), "span, grow, push" );
		this.add( viewer );

	}

	private void populateBdv() {
		if(model == null) return;
		bdvRemoveAll();
		if(data != null) {
			bdvAdd( data, "RAW" );
		}
		if(model.getLabels() == null) return;

		updateLUT();
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(getLUT()[i.get()]);
		RandomAccessibleInterval converted = Converters.convert(model.getLabels().getIndexImg(), converter, new ARGBType() );

		//TODO make virtual channels work
//		List<LUTChannel> virtualChannels = lutBuilder.getVirtualChannels();
//		if(virtualChannels != null) {
//			List<BdvVirtualChannelSource> sources = BdvFunctions.show(
//					converted,
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
					converted,
					"solution",
					Bdv.options().addTo(bdvGetHandlePanel()));
//		}
	}

	private int[] getLUT() {
		return lut;
	}

	private void bdvAdd(
			final RandomAccessibleInterval< T > img,
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
		return this.bdvSources;
	}

	public LUTBuilder<U> getLUTBuilder() {
		return lutBuilder;
	}

	public void setTagColor(Object tag, int color){
		lutBuilder.setColor(tag, color);
		updateLUT();
	}

	public void removeTagColor(Object tag){
		lutBuilder.removeColor(tag);
		updateLUT();
	}

	public LabelEditorModel<U> getModel() {
		return model;
	}
}
