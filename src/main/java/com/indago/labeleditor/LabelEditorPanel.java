package com.indago.labeleditor;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvSource;
import com.indago.labeleditor.display.DefaultLUTBuilder;
import com.indago.labeleditor.display.LUTBuilder;
import com.indago.labeleditor.model.DefaultLabelEditorModel;
import com.indago.labeleditor.model.LabelEditorModel;
import com.indago.labeleditor.model.LabelEditorTag;
import net.imagej.ImgPlus;
import net.imglib2.IterableInterval;
import net.imglib2.Localizable;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;
import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LabelEditorPanel<T extends RealType<T>, U> extends JPanel implements ActionListener {

	private static final long serialVersionUID = -2148493794258482330L;
	private LabelEditorModel model;
	private ImgPlus<T> data;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private MouseMotionListener mml;

	private LUTBuilder<U> lutBuilder;
	private int[] lut;

	public LabelEditorPanel() {
		lutBuilder = initLUTBuilder();
	}

	public LabelEditorPanel( ImgPlus<T> data) {
		this(new DefaultLabelEditorModel<>());
		this.data = data;
	}

	public LabelEditorPanel( ImgPlus< T > data, ImgLabeling< U, IntType > labels) {
		this( new DefaultLabelEditorModel<>( labels ) );
		this.data = data;
	}

	public LabelEditorPanel( ImgPlus< T > data, List< ImgLabeling< U, IntType > > labels) {
		this(new DefaultLabelEditorModel<>(labels));
		this.data = data;
	}

	public LabelEditorPanel(LabelEditorModel model) {
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

	public void init(LabelEditorModel model) {

		setLayout( new BorderLayout() );

		lutBuilder = initLUTBuilder();

		if(model != null) buildPanelFromModel(model);

	}

	private void buildPanelFromModel(LabelEditorModel model) {
		this.model = model;
		//this limits the BDV navigation to 2D
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		buildGui(config);
		populateBdv();

		this.mml = new MouseMotionListener() {

			int lastSegments;

			@Override
			public void mouseDragged( MouseEvent e ) {

			}

			@Override
			public void mouseMoved( MouseEvent e ) {
				Point pos = getMousePositionInBDV();
				LabelingType<U> segments = getLabelsAtPosition(pos);
				int intIndex;
				try {
					intIndex = segments.getIndex().getInteger();
				} catch(ArrayIndexOutOfBoundsException exc) {return;}
				if(intIndex == lastSegments) return;
				lastSegments = intIndex;
				new Thread(() -> {
					model.removeTag(LabelEditorTag.SELECTED);
					segments.forEach(segment -> model.addTag(segment, LabelEditorTag.SELECTED));
					updateLUT();
				}).start();
			}

		};

		bdvHandlePanel.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( this.mml );
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( bdvHandlePanel.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				(ClickBehaviour) (x, y) -> selectLabels( x, y, bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint() ),
				"browse segments",
				"P" );

	}

	private Point getMousePositionInBDV() {
		RealPoint mousePointer = new RealPoint(3);
		bdvHandlePanel.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
		final int x = ( int ) mousePointer.getFloatPosition( 0 );
		final int y = ( int ) mousePointer.getFloatPosition( 1 );
		int time = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
		return new Point(x, y, time);
	}

	private void updateLUT() {
		lut = lutBuilder.build(model);
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	protected LUTBuilder<U> initLUTBuilder() {
		return new DefaultLUTBuilder<>();
	}

	protected void selectLabels(int x, int y, int time ) {
		Point pos = new Point(x, y, time);
		LabelingType<U> labels = getLabelsAtPosition(pos);
		//TODO do something with labels
	}

	protected LabelingType<U> getLabelsAtPosition(Localizable pos) {
		RandomAccess<LabelingType<U>> ra = model.getLabels().randomAccess();
		ra.setPosition(pos);
		return ra.get();
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

	@Override
	public void actionPerformed( ActionEvent e ) {
//		if ( e.getSource().equals( btnForceSelect ) ) {} else if ( e.getSource().equals( btnForceRemove ) ) {}
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

		final T min = img.randomAccess().get().copy();
		final T max = min.copy();
		computeMinMax( Views.iterable( img ), min, max );
		if(!min.equals(max)) {
			source.setDisplayRangeBounds( Math.min( min.getRealDouble(), 0 ), max.getRealDouble() );
			source.setDisplayRange( min.getRealDouble(), max.getRealDouble() );
		}
		source.setActive( true );
	}

	private void computeMinMax(
			final IterableInterval<T> iterableInterval,
			final T min,
			final T max) {
		if ( iterableInterval == null ) { return; }

		// create a cursor for the image (the order does not matter)
		final Iterator< T > iterator = iterableInterval.iterator();

		// initialize min and max with the first image value
		T type = iterator.next();

		min.set( type );
		max.set( type );

		// loop over the rest of the data and determine min and max value
		while ( iterator.hasNext() ) {
			// we need this type more than once
			type = iterator.next();

			if ( type.compareTo( min ) < 0 ) min.set( type );

			if ( type.compareTo( max ) > 0 ) max.set( type );
		}
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
	}

	public void removeTagColor(Object tag){
		lutBuilder.removeColor(tag);
	}

}
