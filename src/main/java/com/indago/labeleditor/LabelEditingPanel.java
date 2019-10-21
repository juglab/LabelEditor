package com.indago.labeleditor;

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
import com.indago.metaseg.ui.model.MetaSegSolverModel;
import com.indago.metaseg.ui.util.SolutionVisualizer;
import com.indago.ui.bdv.BdvWithOverlaysOwner;

import bdv.util.Bdv;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOverlay;
import bdv.util.BdvSource;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.converter.Converters;
import net.imglib2.interpolation.randomaccess.NearestNeighborInterpolatorFactory;
import net.imglib2.roi.labeling.ImgLabeling;
import net.imglib2.roi.labeling.LabelingType;
import net.imglib2.type.BooleanType;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.util.ConstantUtils;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;
import net.miginfocom.swing.MigLayout;

public class LabelEditingPanel extends JPanel implements ActionListener, BdvWithOverlaysOwner {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2148493794258482330L;
	private final MetaSegSolverModel model;
	private JButton btnForceSelect;
	private JButton btnForceRemove;
	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private List< BdvSource > bdvOverlaySources = new ArrayList<>();
	private List< BdvOverlay > overlays = new ArrayList<>();
	private MouseMotionListener mml;
	protected ImgLabeling< LabelData, IntType > labelingFrames;
	protected RealPoint mousePointer;
	private ArrayList< LabelingSegment > segmentsUnderMouse;
	private int selectedIndex;
	private ValuePair< LabelingSegment, Integer > highlightedSegment;
	private final LinkedRandomAccessible< IntType > highlightedSegmentRai =
			new LinkedRandomAccessible<>( ConstantUtils.constantRandomAccessible( new IntType(), 2 ) ); //TODO Change 2 to match 2D/3D image

	public LabelEditorPanel( MetaSegSolverModel solutionModel ) {
		super( new BorderLayout() );
		this.model = solutionModel;
		buildGui();
		populateBdv();
		this.mml = new MouseMotionListener() {

			@Override
			public void mouseDragged( MouseEvent e ) {
			}

			@Override
			public void mouseMoved( MouseEvent e ) {
				mousePointer = new RealPoint( 3 );
				bdvHandlePanel.getViewerPanel().getGlobalMouseCoordinates( mousePointer );
				final int x = ( int ) mousePointer.getFloatPosition( 0 );
				final int y = ( int ) mousePointer.getFloatPosition( 1 );
				final int z = ( int ) mousePointer.getFloatPosition( 2 );
				if ( !model.getPgSolutions().isEmpty() && !( model.getPgSolutions() == null ) ) {
					int time = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
					labelingFrames =
							model.getModel().getCostTrainerModel().getLabelings().getLabelingPlusForFrame( time ).getLabeling();
					findSegments( x, y, z, time );
					if ( !( segmentsUnderMouse.isEmpty() ) ) {
						highlightedSegment = new ValuePair< LabelingSegment, Integer >( segmentsUnderMouse.get( 0 ), time );
						JComponent component = ( JComponent ) e.getSource();
						LabelingSegment ls = highlightedSegment.getA();
						component.setToolTipText( "Cost of segment: " + model.getModel().getCostTrainerModel().getCost( ls ) );
						showHighlightedSegment();
						setSelectedIndex( 0 );
					}

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
		highlightedSegmentRai.setSource( ret );
		bdvHandlePanel.getViewerPanel().setTimepoint( highlightedSegment.getB() );
		bdvHandlePanel.getViewerPanel().requestRepaint();
	}

	protected ArrayList< LabelingSegment > findSegments( final int x, final int y, int z, int time ) {
		segmentsUnderMouse = new ArrayList<>();
		final RealRandomAccess< LabelingType< LabelData > > a = Views
				.interpolate(
						Views.extendValue( labelingFrames, labelingFrames.firstElement().createVariable() ),
						new NearestNeighborInterpolatorFactory<>() )
				.realRandomAccess();

		a.setPosition( new int[] { x, y } );
		for ( LabelData labelData : a.get() ) {
			segmentsUnderMouse.add( labelData.getSegment() );
		}
		return segmentsUnderMouse;

	}


	private void buildGui() {
		final JPanel viewer = new JPanel( new BorderLayout() );

		if ( model.getModel().is2D() ) {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv
					.options()
					.is2D()
					.inputTriggerConfig( model.getModel().getDefaultInputTriggerConfig() ) );
		} else {
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), Bdv
					.options() );
		}
		//This gives 2D/3D bdv panel for leveraged editing
		bdvAdd( model.getRawData(), "RAW" );
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
		if ( e.getSource().equals( btnForceSelect ) ) {
	}else if (e.getSource().equals( btnForceRemove )) {
		}
	}

	public void populateBdv() {
		bdvRemoveAll();
		bdvAdd( model.getRawData(), "RAW" );
		final int bdvTime = bdvHandlePanel.getViewerPanel().getState().getCurrentTimepoint();
		if ( model.getPgSolutions() != null && model.getPgSolutions().size() > bdvTime && model.getPgSolutions().get( bdvTime ) != null ) {
			RandomAccessibleInterval< IntType > imgSolution = SolutionVisualizer.drawSolutionSegmentImages( this.model );
			bdvAdd( imgSolution, "solution", 0, 2, new ARGBType( 0x00FF00 ), true );
		}
		bdvAdd(
				Views.interval( Views.addDimension( highlightedSegmentRai ), model.getRawData() ),
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

	@Override
	public List< BdvSource > bdvGetOverlaySources() {
		return this.bdvOverlaySources;
	}

	@Override
	public List< BdvOverlay > bdvGetOverlays() {
		return this.overlays;
	}
}
