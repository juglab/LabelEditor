package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.util.VirtualChannels;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import sc.fiji.labeleditor.core.AbstractLabelEditorPanel;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.colors.LabelEditorColorset;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LabelEditorBdvPanel<L> extends AbstractLabelEditorPanel {

	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();

	@Parameter
	private Context context;
	private boolean mode3D = false;

	@Override
	protected void initController() {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel, bdvSources, view());
		control().init(model(), view(), viewerInstance);
		addBehaviours(control());
		control().interfaceInstance().set3DViewMode(is3DMode());
	}

	private boolean is3DMode() {
		return mode3D;
	}

	@Override
	protected Component buildInterface() {
		InputTriggerConfig config = new InputTriggerConfig2D().load(this);
		BdvOptions options = Bdv.options().accumulateProjectorFactory(LabelEditorAccumulateProjector.factory);
		if(!is3DMode() && config != null ) {
			System.out.println("2D mode");
			bdvHandlePanel = new BdvHandlePanel(getFrame(), options.is2D().inputTriggerConfig(config));
		} else {
			System.out.println("3D mode");
			bdvHandlePanel = new BdvHandlePanel( getFrame(), options);
		}
		return bdvHandlePanel.getViewerPanel();
	}

	private Frame getFrame() {
		Container topLevelAncestor = SwingUtilities.getWindowAncestor(this);
		if(topLevelAncestor == null) return null;
		if(topLevelAncestor.getClass().isAssignableFrom(JDialog.class)) {
			topLevelAncestor = SwingUtilities.getWindowAncestor(topLevelAncestor);
		}
		return (Frame) topLevelAncestor;
	}

	@Override
	protected void addBehaviours(LabelEditorController controller) {
		controller.addDefaultBehaviours();
	}

	@Override
	protected void displayLabeling() {
		if(view().renderers().size() == 0) return;
		//TODO finish integrating virtual channels to change colors
//		Collection<LabelEditorColorset> virtualChannels = model().colors().getVirtualChannels();
//		if(virtualChannels != null) {
//			view().renderers().forEach(renderer -> displayInBdv(renderer.getOutput(), virtualChannels, renderer.getName()));
//		} else {
			view().renderers().forEach(renderer -> displayInBdv(renderer.getOutput(), renderer.getName()));
//		}
	}

	@Override
	protected void displayData(RandomAccessibleInterval data) {
		if(data != null) {
			displayInBdv( data, "source" );
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

	private void displayInBdv(final RandomAccessibleInterval img, Collection<LabelEditorColorset> colorsets,
	                          final String title ) {
		List<VirtualChannels.VirtualChannel> virtualChannels = new ArrayList<>();
		List<LabelEditorColorset> listColorsets = new ArrayList<>();
		listColorsets.addAll(colorsets);
		virtualChannels.addAll(listColorsets);
		List<BdvSource> source = BdvFunctions.show(img, virtualChannels, title, Bdv.options().addTo(getInterfaceHandle()));
		getSources().addAll( source );
//		for (int i = 0; i < listColorsets.size(); i++) {
//			getInterfaceHandle().getBdvHandle().getSetupAssignments().getConverterSetups().get(i).getColor()
//		}
		source.forEach(s -> s.setActive(true) );
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

	public void setMode3D(boolean set3D) {
		mode3D = true;
	}
}
