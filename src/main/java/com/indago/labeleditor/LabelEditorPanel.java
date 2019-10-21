package com.indago.labeleditor;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.scijava.ui.behaviour.ClickBehaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Behaviours;

import com.indago.data.segmentation.LabelData;
import com.indago.data.segmentation.LabelingSegment;
import com.indago.fg.Assignment;
import com.indago.io.DataMover;
import com.indago.pg.IndicatorNode;
import com.indago.pg.SegmentationProblem;
import com.indago.pg.segments.SegmentNode;
import com.indago.ui.bdv.BdvOwner;
import com.indago.ui.bdv.BdvWithOverlaysOwner;
import com.indago.util.ImglibUtil;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOverlay;
import bdv.util.BdvSource;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.converter.Converters;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.roi.IterableRegion;
import net.imglib2.roi.Regions;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.ValuePair;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;

public class LabelEditorPanel extends JPanel implements ActionListener, BdvOwner {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2148493794258482330L;
	private final ImgPlus< ? > data;
	private JButton btnForceSelect;
	private JButton btnForceRemove;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private List< BdvSource > bdvOverlaySources = new ArrayList<>();
	private List< BdvOverlay > overlays = new ArrayList<>();
	private MouseMotionListener mml;
	//protected ImgLabeling< LabelData, IntType > labelingFrames;
	protected ImgLabeling< ?, ? > labelingFrames;
	protected RealPoint mousePointer;
	private ArrayList< LabelingSegment > segmentsUnderMouse;
	private int selectedIndex;
	private ValuePair< LabelingSegment, Integer > highlightedSegment;
	private final RandomAccessible< IntType > highlightedSegmentRai =
			ConstantUtils.constantRandomAccessible( new IntType(), 2 ); //TODO Change 2 to match 2D/3D image
	private List< ImgLabeling< ?, ? > > labels;
	private List<Assignment< IndicatorNode >> pgSolutions;
	private List< SegmentationProblem > problems;

	public LabelEditorPanel( ImgPlus< ? > data, List< ImgLabeling< ?, ? > > labels, InputTriggerConfig config, List<Assignment< IndicatorNode >> pgSolutions, List<SegmentationProblem> problems) {
		setLayout( new BorderLayout() );
		this.data = data;
		this.labels = labels;
		this.pgSolutions = pgSolutions;
		this.problems = problems;
		buildGui(config);
		populateBdv();
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
				// Perhaps this need to be more generic, ie is it always IntType?
				labelingFrames = ( ImgLabeling< ?, ? > ) labels.get( time );
				findSegments( x, y, z, time );
				if ( !( segmentsUnderMouse.isEmpty() ) ) {
					highlightedSegment = new ValuePair< LabelingSegment, Integer >( segmentsUnderMouse.get( 0 ), time );
					JComponent component = ( JComponent ) e.getSource();
					LabelingSegment ls = highlightedSegment.getA();
					// Too specific... need to figure something else out....like the labels should be "rich" ie have additional info 
					// that if there can also be rendered, probably not in a tool tip. 
					// instead, we should have options to turn this extra info on and off, in case the view gets too crowded.
					// component.setToolTipText( "Cost of segment: " + data.getdata().getCostTrainerdata().getCost( ls ) );
					showHighlightedSegment();
					setSelectedIndex( 0 );

				}

			}

		};
		bdvHandlePanel.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener( this.mml );
		final Behaviours behaviours = new Behaviours( new InputTriggerConfig(), new String[] { "metaseg" } );
		behaviours.install( bdvHandlePanel.getBdvHandle().getTriggerbindings(), "my-new-behaviours" );
		behaviours.behaviour(
				new ClickBehaviour() {

					@Override
					public void click( int x, int y ) {
						browseSegments( x, y, bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint() );

					}
				},
				"browse segments",
				"P" );
	}

	protected void browseSegments( int x, int y, int time ) {
		if ( !( segmentsUnderMouse == null ) && !( segmentsUnderMouse.isEmpty() ) ) {
			if ( segmentsUnderMouse.size() > 1 ) {
				int idx = getSelectedIndex();
				if ( idx == segmentsUnderMouse.size() - 1 ) {
					setSelectedIndex( 0 );
					highlightedSegment = new ValuePair< LabelingSegment, Integer >( segmentsUnderMouse.get( selectedIndex ), time );
					showHighlightedSegment();
				} else {
					setSelectedIndex( idx + 1 );
					highlightedSegment = new ValuePair< LabelingSegment, Integer >( segmentsUnderMouse.get( selectedIndex ), time );
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
		RandomAccessibleInterval< ? extends BooleanType< ? > > region = highlightedSegment.getA().getRegion();
		RandomAccessibleInterval< IntType > ret = Converters.convert( region, ( in, out ) -> out.set( in.get() ? 1 : 0 ), new IntType() );
		( ( AWTEvent ) highlightedSegmentRai ).setSource( ret );
		bdvHandlePanel.getViewerPanel().setTimepoint( highlightedSegment.getB() );
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	protected ArrayList< LabelingSegment > findSegments( final int x, final int y, int z, int time ) {
		segmentsUnderMouse = new ArrayList<>();
		final RealRandomAccess< LabelingType< LabelData > > a = Views.interpolate(
				Views.extendValue( labelingFrames, labelingFrames.firstElement().createVariable() ),
				new NearestNeighborInterpolatorFactory<>() ).realRandomAccess();

		a.setPosition( new int[] { x, y } );
		for ( LabelData labelData : a.get() ) {
			segmentsUnderMouse.add( labelData.getSegment() );
		}
		return segmentsUnderMouse;

	}

	private void buildGui(InputTriggerConfig config) {
		final JPanel viewer = new JPanel( new BorderLayout() );

		// TODO... How do you find out what kind of data it is? A utility perhaps or is it good enough to check that config is not null? Can it be null in a generic case?
		if ( /*data.is2D()*/ config != null ) {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options().is2D().inputTriggerConfig(config));
		} else {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv.options() );
		}
		//This gives 2D/3D bdv panel for leveraged editing
		bdvAdd( (RandomAccessibleInterval< ? >)data, "RAW" );
		viewer.add( bdvHandlePanel.getViewerPanel(), BorderLayout.CENTER );

		final MigLayout layout = new MigLayout( "", "[][grow]", "" );
		final JPanel controls = new JPanel( layout );

		final JPanel panelEdit = new JPanel( new MigLayout() );
		panelEdit.setBorder( BorderFactory.createTitledBorder( "leveraged editing" ) );

		btnForceSelect = new JButton( "force select" );
		btnForceSelect.addActionListener( this );
		btnForceRemove = new JButton( "force remove" );
		btnForceRemove.addActionListener( this );
		panelEdit.add( btnForceSelect, "growx, wrap" );
		panelEdit.add( btnForceRemove, "growx, wrap" );

		controls.add( panelEdit, "growx, wrap" );

		final JSplitPane splitPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, controls, viewer );
		splitPane.setResizeWeight( 0.1 ); // 1.0 == extra space given to left component alone!
		this.add( splitPane, BorderLayout.CENTER );

	}

	@Override
	public void actionPerformed( ActionEvent e ) {
		if ( e.getSource().equals( btnForceSelect ) ) {} else if ( e.getSource().equals( btnForceRemove ) ) {}
	}

	public void populateBdv() {
		bdvRemoveAll();
		bdvAdd( ( RandomAccessibleInterval< ? > ) data, "RAW" );
		final int bdvTime = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
		RandomAccessibleInterval< IntType > imgSolution = drawSolutionSegmentImages();
		bdvAdd( imgSolution, "solution", 0, 2, new ARGBType( 0x00FF00 ), true );
		bdvAdd(
				Views.interval( Views.addDimension( highlightedSegmentRai ), data ),
				"lev. edit",
				0,
				2,
				new ARGBType( 0x00BFFF ),
				true );
	}

	@Override
	public BdvHandlePanel bdvGetHandlePanel() {
		return bdvHandlePanel;
	}

	@Override
	public void bdvSetHandlePanel( final BdvHandlePanel bdvHandlePanel ) {
		this.bdvHandlePanel = bdvHandlePanel;
	}

	@Override
	public List< BdvSource > bdvGetSources() {
		return this.bdvSources;
	}

	@Override
	public < T extends RealType< T > & NativeType< T > > BdvSource bdvGetSourceFor( RandomAccessibleInterval< T > img ) {
		return null;
	}

	public List< BdvSource > bdvGetOverlaySources() {
		return this.bdvOverlaySources;
	}

	public List< BdvOverlay > bdvGetOverlays() {
		return this.overlays;
	}

	private RandomAccessibleInterval< IntType > drawSolutionSegmentImages( ) {

		final RandomAccessibleInterval< IntType > ret =
				DataMover.createEmptyArrayImgLike( data, new IntType() );

		// TODO: must check data type, how? Methods left in place to convey logic, need to update to real objects
		if ( data.hasFrames() ) {
			for ( int t = 0; t < data.getNumberOfFrames(); t++ ) {
				final Assignment< IndicatorNode > solution = pgSolutions.get( t );
				if ( solution != null ) {
					final IntervalView< IntType > retSlice = Views.hyperSlice( ret, data.getTimeDimensionIndex(), t );

					final int curColorId = 1;
					for ( final SegmentNode segVar : problems.get( t ).getSegments() ) {
						if ( solution.getAssignment( segVar ) == 1 ) {
							drawSegmentWithId( retSlice, solution, segVar, curColorId );
						}
					}
				}
			}
		} else {
			final Assignment< IndicatorNode > solution = pgSolutions.get( 0 );
			if ( solution != null ) {
				final int curColorId = 1;
				for ( final SegmentNode segVar : problems.get( 0 ).getSegments() ) {
					if ( solution.getAssignment( segVar ) == 1 ) {
						drawSegmentWithId( ret, solution, segVar, curColorId );
					}
				}
			}
		}
		return ret;
	}

	private static void drawSegmentWithId(
			final RandomAccessibleInterval< IntType > imgSolution,
			final Assignment< IndicatorNode > solution,
			final SegmentNode segVar,
			final int curColorId ) {

		if ( solution.getAssignment( segVar ) == 1 ) {
			final int color = curColorId;

			final IterableRegion< ? > region = segVar.getSegment().getRegion();
			final int c = color;
			try {
				Regions.sample( region, imgSolution ).forEach( t -> t.set( c ) );
			} catch ( final ArrayIndexOutOfBoundsException aiaob ) {
				//TODO: log.error( aiaob );
			}
		}
	}
	
	private RandomAccessibleInterval< DoubleType > getFrame( final long t ) {
		final int timeIdx = ImglibUtil.getTimeDimensionIndex( data);
		if ( timeIdx == -1 ) {
			return data;
		} else {
			return Views.hyperSlice( data, timeIdx, data.min( timeIdx ) + t );
		}

	}


}
