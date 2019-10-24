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
import io.scif.img.IO;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.Intervals;
import net.imglib2.util.ValuePair;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LabelEditorPanel<T extends RealType<T>, U> extends JPanel implements ActionListener {

	private static final long serialVersionUID = -2148493794258482330L;
	private LabelEditorModel model;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private MouseMotionListener mml;
	protected RealPoint mousePointer;
	private ArrayList< U > segmentsUnderMouse;
	private int selectedIndex;
	private ValuePair< U, Integer > highlightedSegment;

	protected LUTBuilder<U> lutBuilder;
	int[] lut;

	public LabelEditorPanel() {
	}

	public LabelEditorPanel( ImgPlus< T > data, ImgLabeling< U, IntType > labels) {
		this(data, Collections.singletonList(labels));
	}

	public LabelEditorPanel( ImgPlus< T > data, List< ImgLabeling< U, IntType > > labels) {
		this(new DefaultLabelEditorModel<>(data, labels));
	}

	public LabelEditorPanel(LabelEditorModel model) {
		init(model);
	}

	public void init(ImgPlus<T> data, ImgLabeling<U, IntType> labels) {
		init(new DefaultLabelEditorModel<>(data, labels));
	}

	public void init(LabelEditorModel model) {

		setLayout( new BorderLayout() );

		lutBuilder = initLUTBuilder();

		if(model != null) buildPanelFromModel(model);

		this.mml = new MouseMotionListener() {

			@Override
			public void mouseDragged( MouseEvent e ) {}

			@Override
			public void mouseMoved( MouseEvent e ) {
				mousePointer = new RealPoint( 3 );
				bdvHandlePanel.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
				final int x = ( int ) mousePointer.getFloatPosition( 0 );
				final int y = ( int ) mousePointer.getFloatPosition( 1 );
				final int z = ( int ) mousePointer.getFloatPosition( 2 );
				int time = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
				findSegments( x, y, z, time );
				if ( !( segmentsUnderMouse.isEmpty() ) ) {
					highlightedSegment = new ValuePair< >( segmentsUnderMouse.get( 0 ), time );
					JComponent component = ( JComponent ) e.getSource();
					U ls = highlightedSegment.getA();
					// Too specific... need to figure something else out....like the labels should be "rich" ie have additional info
					// that if there can also be rendered, probably not in a tool tip.
					// instead, we should have options to turn this extra info on and off, in case the view gets too crowded.
					// component.setToolTipText( "Cost of segment: " + data.getdata().getCostTrainerdata().getCost( ls ) );
//					showHighlightedSegment();
//					setSelectedIndex( 0 );
					model.addTag(time, ls, LabelEditorTag.VISIBLE);
				}

			}

		};
	}

	private void buildPanelFromModel(LabelEditorModel model) {
		this.model = model;
		//this limits the BDV navigation to 2D
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		buildGui(config);
		populateBdv();
		bdvHandlePanel.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( this.mml );
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), "metaseg");
		behaviours.install( bdvHandlePanel.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				(ClickBehaviour) (x, y) -> browseSegments( x, y, bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint() ),
				"browse segments",
				"P" );
	}

	protected LUTBuilder<U> initLUTBuilder() {
		return new DefaultLUTBuilder<>();
	}

	protected void browseSegments( int x, int y, int time ) {
		if ( !( segmentsUnderMouse == null ) && !( segmentsUnderMouse.isEmpty() ) ) {
			if ( segmentsUnderMouse.size() > 1 ) {
				int idx = getSelectedIndex();
				if ( idx == segmentsUnderMouse.size() - 1 ) {
					setSelectedIndex( 0 );
					highlightedSegment = new ValuePair<>(segmentsUnderMouse.get(selectedIndex), time);
					showHighlightedSegment();
				} else {
					setSelectedIndex( idx + 1 );
					highlightedSegment = new ValuePair<>(segmentsUnderMouse.get(selectedIndex), time);
					showHighlightedSegment();
				}
			}
		}

	}

	protected void setSelectedIndex( int i ) {
		selectedIndex = i;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	protected void showHighlightedSegment() {
		//FIXME
//		RandomAccessibleInterval< ? extends BooleanType< ? > > region = highlightedSegment.getA().getRegion();
//		RandomAccessibleInterval< IntType > ret = Converters.convert( region, ( in, out ) -> out.set( in.get() ? 1 : 0 ), new IntType() );
//		( ( AWTEvent ) highlightedSegmentRai ).setSource( ret );
//		bdvHandlePanel.getViewerPanel().setTimepoint( highlightedSegment.getB() );
//		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	protected ArrayList< U > findSegments( final int x, final int y, int z, int time ) {
		segmentsUnderMouse = new ArrayList<>();
		//FIXME
		//		final RealRandomAccess< LabelingType< U > > a = Views.interpolate(
//				Views.extendValue( model.getLabels( time ), model.getLabels( time ).firstElement().createVariable() ),
//				new NearestNeighborInterpolatorFactory<>() ).realRandomAccess();
//
//		a.setPosition( new int[] { x, y } );
//		for ( LabelingSegment labelData : a.get() ) {
//			segmentsUnderMouse.add( labelData );
//		}
		return segmentsUnderMouse;

	}

	private void buildGui(InputTriggerConfig config) {
		final JPanel viewer = new JPanel( new MigLayout("fill, w 500, h 500") );

		// TODO... How do you find out what kind of data it is? A utility perhaps or is it good enough to check that config is not null? Can it be null in a generic case?
		if ( /*data.is2D()*/ config != null ) {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options().is2D().inputTriggerConfig(config));
		} else {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options() );
		}
		//This gives 2D/3D bdv panel for leveraged editing
		bdvAdd( model.getData(), "RAW" );
		viewer.add( bdvHandlePanel.getViewerPanel(), "span, grow, push" );

		this.add( viewer );

	}

	@Override
	public void actionPerformed( ActionEvent e ) {
//		if ( e.getSource().equals( btnForceSelect ) ) {} else if ( e.getSource().equals( btnForceRemove ) ) {}
	}

	public void populateBdv() {
		if(model == null) return;
		bdvRemoveAll();
		bdvAdd( model.getData(), "RAW" );
		final int bdvTime = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
		ImgLabeling<U, IntType> img = model.getLabels(bdvTime);
		lut = lutBuilder.build(img, model.getTags(bdvTime));
		Converter<IntType, ARGBType> converter = (i, o) -> o.set(lut[i.get()]);

		RandomAccessibleInterval converted = Converters.convert(img.getIndexImg(), converter, new ARGBType() );

		final BdvSource source = BdvFunctions.show(
				converted,
				"solution",
				Bdv.options().addTo( bdvGetHandlePanel() ) );
		source.setActive(true);
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
//	private RandomAccessibleInterval< IntType > drawSolutionSegmentImages( ) {
//
//		LabelEditorTag approved = new LabelEditorTag("approved");
//
//		final RandomAccessibleInterval< IntType > ret =
//				DataMover.createEmptyArrayImgLike( model.getData(), new IntType() );
//
//		// TODO: must check data type, how? Methods left in place to convey logic, need to update to real objects
//		int timeDim = model.getData().dimensionIndex(Axes.TIME);
//		if ( timeDim < 0 ) {
//			for ( int t = 0; t < model.getData().dimension(timeDim); t++ ) {
//				final Assignment< IndicatorNode > solution = pgSolutions.get( t );
//				if ( solution != null ) {
//					final IntervalView< IntType > retSlice = Views.hyperSlice( ret, timeDim, t );
//
//					final int curColorId = 1;
//					for ( final SegmentNode segVar : problems.get( t ).getSegments() ) {
//						if ( solution.getAssignment( segVar ) == 1 ) {
//							model.setTag(segVar.getSegment(), approved);
////							drawSegmentWithId( retSlice, solution, segVar, curColorId );
//						}
//					}
//				}
//			}
//		} else {
//			final Assignment< IndicatorNode > solution = pgSolutions.get( 0 );
//			if ( solution != null ) {
//				final int curColorId = 1;
//				for ( final SegmentNode segVar : problems.get( 0 ).getSegments() ) {
//					if ( solution.getAssignment( segVar ) == 1 ) {
//						model.setTag(segVar.getSegment(), approved);
////						drawSegmentWithId( ret, solution, segVar, curColorId );
//					}
//				}
//			}
//		}
//		return ret;

//	}
//	private static void drawSegmentWithId(
//			final RandomAccessibleInterval< IntType > imgSolution,
//			final Assignment< IndicatorNode > solution,
//			final SegmentNode segVar,
//			final int curColorId ) {
//
//		if ( solution.getAssignment( segVar ) == 1 ) {
//			final int color = curColorId;
//
//			final IterableRegion< ? > region = segVar.getSegment().getRegion();
//			final int c = color;
//			try {
//				Regions.sample( region, imgSolution ).forEach( t -> t.set( c ) );
//			} catch ( final ArrayIndexOutOfBoundsException aiaob ) {
//				//TODO: log.error( aiaob );
//			}
//		}

//	}

	public LUTBuilder<U> getLUTBuilder() {
		return lutBuilder;
	}

	public static void main(String... args) {
		DefaultLabelEditorModel model = buildModel();
		JFrame frame = new JFrame("Label editor");
		JPanel parent = new JPanel();
		frame.setContentPane(parent);
		frame.setMinimumSize(new Dimension(500,500));
		LabelEditorPanel labelEditorPanel = new LabelEditorPanel<>(model);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		parent.add(labelEditorPanel);
		frame.pack();
		frame.setVisible(true);
	}

	private static <T extends RealType<T> & NativeType<T>> DefaultLabelEditorModel<T, String> buildModel() {
		Img input = IO.openImgs(LabelEditorPanel.class.getResource("/raw.tif").getPath()).get(0);
		ImgPlus<T> data = new ImgPlus<T>(input, "input", new AxisType[]{Axes.X, Axes.Y, Axes.TIME});

		ArrayImg<IntType, IntArray> backing = ArrayImgs.ints( data.dimension(0), data.dimension(1) );
		ImgLabeling< String, IntType > labels = new ImgLabeling<>( backing );
		String LABEL1 = "label1";
		String LABEL2 = "label2";

		Views.interval( labels, Intervals.createMinSize( 20, 20, 100, 100 ) ).forEach(pixel -> pixel.add( LABEL1 ) );
		Views.interval( labels, Intervals.createMinSize( 80, 80, 100, 100 ) ).forEach( pixel -> pixel.add( LABEL2 ) );

		DefaultLabelEditorModel<T, String> model = new DefaultLabelEditorModel<>(data, labels);

		model.addTag(0, LABEL1, LabelEditorTag.SELECTED);
		return model;
	}
}
