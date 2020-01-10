package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import sc.fiji.labeleditor.core.AbstractLabelEditorPanel;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LabelEditorBdvPanel extends AbstractLabelEditorPanel {

	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();

	@Parameter
	private Context context;
	private boolean mode3D = false;

	@Override
	protected <L> void initController(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel, bdvSources, view);
		control.init(model, view, viewerInstance);
		addBehaviours(control);
		control.interfaceInstance().set3DViewMode(is3DMode());
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
	protected void display(LabelEditorView view) {
		if(view.renderers().size() == 0) return;
		view.renderers().forEach(renderer -> displayInBdv(renderer.getOutput(), renderer.getName()));
	}

	@Override
	protected void display(RandomAccessibleInterval data) {
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

	@Override
	public BdvHandlePanel getInterfaceHandle() {
		return bdvHandlePanel;
	}

	public List< BdvSource > getSources() {
		return bdvSources;
	}

	@Override
	public void dispose() {
		super.dispose();
		if(getInterfaceHandle() != null) getInterfaceHandle().close();
		bdvHandlePanel.close();
	}

	public void setMode3D(boolean set3D) {
		mode3D = true;
	}
}
