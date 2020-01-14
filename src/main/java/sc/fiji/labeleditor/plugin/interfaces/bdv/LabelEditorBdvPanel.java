package sc.fiji.labeleditor.plugin.interfaces.bdv;

import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import bdv.util.BdvHandlePanel;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import bdv.viewer.state.SourceGroup;
import net.imglib2.RandomAccessibleInterval;
import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.Disposable;
import org.scijava.command.CommandService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import sc.fiji.labeleditor.core.DefaultInteractiveLabeling;
import sc.fiji.labeleditor.core.InteractiveLabeling;
import sc.fiji.labeleditor.core.controller.DefaultLabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorController;
import sc.fiji.labeleditor.core.controller.LabelEditorInterface;
import sc.fiji.labeleditor.core.model.LabelEditorModel;
import sc.fiji.labeleditor.core.view.DefaultLabelEditorView;
import sc.fiji.labeleditor.core.view.LabelEditorView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LabelEditorBdvPanel extends JPanel implements Disposable {

	@Parameter
	private Context context;

	@Parameter
	private ObjectService objectService;

	private final Map<String, InteractiveLabeling> labelings = new HashMap<>();

	private boolean initialized = false;


	private BdvHandlePanel bdvHandlePanel;
	private List< BdvSource > bdvSources = new ArrayList<>();
	private final Map<String, SourceGroup> sourceGroups = new HashMap<>();

	@Parameter
	private CommandService commandService;

	private boolean mode3D = false;

	private void initialize() {
		setMinimumSize(new Dimension(100, 100));
		setPreferredSize(new Dimension(500, 500));
		setLayout( new MigLayout("fill") );
		this.add( buildInterface(), "span, grow, push" );
	}

	public <L> InteractiveLabeling add(LabelEditorModel<L> model) {
		SourceGroup group = new SourceGroup(model.getName());
		sourceGroups.put(model.getName(), group);
		if(!initialized) {
			initialized = true;
			initialize();
		}
		LabelEditorView<L> view = initView(model);
		LabelEditorController<L> control = initControl(model, view);
		display(model.getData(), "data", model.getName());
		if(view.renderers().size() > 0) {
			view.renderers().forEach(renderer -> display(renderer.getOutput(), renderer.getName(), model.getName()));
		}
		InteractiveLabeling labeling = createAndRegisterInteractiveLabeling(model, view, control);
		System.out.println("Created LabelEditor panel of type " + getClass().getName() + " with model:\n" + model.toString());
		return labeling;
	}

	private <L> InteractiveLabeling createAndRegisterInteractiveLabeling(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		InteractiveLabeling labeling = new DefaultInteractiveLabeling(model, view, control);
		labelings.put(model.getName(), labeling);
		if(objectService != null) {
			objectService.addObject(labeling);
		}
		return labeling;
	}

	private <L> LabelEditorView<L> initView(LabelEditorModel<L> model) {
		LabelEditorView<L> view = createView();
		view.init(model);
		if(context() != null) {
			context().inject(view.renderers());
		}
		addRenderers(view);
		return view;
	}

	protected <L> LabelEditorView<L> createView() {
		return new DefaultLabelEditorView<>();
	}

	private <L> LabelEditorController<L> initControl(LabelEditorModel<L> model, LabelEditorView<L> view) {
		LabelEditorController<L> control = createController();
		if(context() != null) {
			context().inject(control);
		}
		initController(model, view, control);
		return control;
	}

	protected <L> LabelEditorController<L> createController() {
		return new DefaultLabelEditorController<>();
	}

	protected void addRenderers(LabelEditorView view) {
		view.renderers().addDefaultRenderers();
	}

	protected Context context() {
		return context;
	}

	public Container get() {
		return this;
	}

	private <L> void initController(LabelEditorModel<L> model, LabelEditorView<L> view, LabelEditorController<L> control) {
		LabelEditorInterface<L> viewerInstance = new BdvInterface<>(bdvHandlePanel, bdvSources, view);
		control.init(model, view, viewerInstance);
		addBehaviours(control);
		control.interfaceInstance().set3DViewMode(is3DMode());
	}

	private boolean is3DMode() {
		return mode3D;
	}

	private Component buildInterface() {
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

	private void addBehaviours(LabelEditorController controller) {
		controller.addDefaultBehaviours();
	}

	private void display(RandomAccessibleInterval rai, String title, String groupName) {
		if(rai != null) {
			displayInBdv(rai, title, groupName );
		}
	}

	private void displayInBdv(final RandomAccessibleInterval rai,
	                          String title, final String groupName) {
		final BdvSource source = BdvFunctions.show(
				rai,
				title,
				Bdv.options().addTo( bdvHandlePanel ) );
		getSources().add( source );
		source.setActive( true );
	}

	public List< BdvSource > getSources() {
		return bdvSources;
	}

	@Override
	public void dispose() {
		labelings.forEach((name,  labeling) -> labeling.view().dispose());
		if(bdvHandlePanel != null) bdvHandlePanel.close();
	}

	public void setMode3D(boolean set3D) {
		mode3D = true;
	}
}
