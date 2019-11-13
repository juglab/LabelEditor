package com.indago.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import com.indago.labeleditor.core.AbstractLabelEditorPanel;
import com.indago.labeleditor.core.controller.LabelEditorController;
import com.indago.labeleditor.core.controller.LabelEditorInterface;
import com.indago.labeleditor.plugin.behaviours.ModificationBehaviours;
import net.imagej.axis.Axes;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class LabelEditorBdvPanel<L> extends AbstractLabelEditorPanel<L> {

	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();

	@Parameter
	Context context;

	@Override
	protected void initController() {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel, bdvSources);
		control().init(model(), view(), viewerInstance);
		addBehaviours(control());
		control().interfaceInstance().set3DViewMode(is3DMode());
	}

	private boolean is3DMode() {
		if(model().getData() == null) return false;
		return model().getData().dimensionIndex(Axes.Z) > 0;
	}

	@Override
	protected Component buildInterface() {
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		BdvOptions options = Bdv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		if(!is3DMode() && config != null ) {
			System.out.println("2D mode");
			bdvHandlePanel = new BdvHandlePanel( (Frame) this.getTopLevelAncestor(), options.is2D().inputTriggerConfig(config));
		} else {
			System.out.println("3D mode");
			bdvHandlePanel = new BdvHandlePanel( ( Frame ) this.getTopLevelAncestor(), options);
		}
		return bdvHandlePanel.getViewerPanel();
	}

	@Override
	protected void addBehaviours(LabelEditorController<L> controller) {
		controller.addDefaultBehaviours();
		ModificationBehaviours modificationBehaviours = new ModificationBehaviours(model(), control());
		if(context != null) context.inject(modificationBehaviours);
		getInterfaceHandle().getViewerPanel().getDisplay().addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				if (e.isPopupTrigger()) {
					BdvPopupMenu menu = new BdvPopupMenu(modificationBehaviours);
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

		});
	}

	@Override
	protected void displayLabeling() {
		if(view().renderers().size() == 0) return;
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
		view().renderers().forEach(renderer -> displayInBdv(renderer.getOutput(), renderer.getName()));
//		}
	}

	@Override
	protected void displayData() {
		if(model().getData() != null) {
			displayInBdv( model().getData(), "RAW" );
		}
	}

	private void displayInBdv( final RandomAccessibleInterval img,
	                           final String title ) {
		final BdvSource source = BdvFunctions.show(
				img,
				title,
				Bdv.options().addTo( getInterfaceHandle() ) );
		getSources().add( source );
		source.setActive( true );
	}

	@Override
	protected void clearInterface() {
		for ( final BdvSource source : getSources()) {
			source.removeFromBdv();
		}
		getSources().clear();
	}

	@Override
	public BdvHandlePanel getInterfaceHandle() {
		return bdvHandlePanel;
	}

	public List< BdvSource > getSources() {
		return bdvSources;
	}

	@Override
	public void dispose() {
		if(getInterfaceHandle() != null) getInterfaceHandle().close();
	}

	class BdvPopupMenu extends JPopupMenu {

		BdvPopupMenu(ModificationBehaviours modificationBehaviours) {
			addMenuItem(actionEvent -> new Thread( () -> modificationBehaviours.getDeleteBehaviour().deleteSelected()).start(), "Delete selected labels");
			addMenuItem(actionEvent -> new Thread( () -> modificationBehaviours.getSplitBehaviour().splitSelected()).start(), "Split selected labels");
		}

		private void addMenuItem(ActionListener actionListener, String label) {
			JMenuItem item = new JMenuItem(label);
			item.addActionListener(actionListener);
			add(item);
		}
	}

}
